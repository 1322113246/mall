package com.lb.gulimail.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lb.common.constant.OrderStatusEnum;
import com.lb.common.to.OrderEntityTo;
import com.lb.common.to.mq.StockDetailTo;
import com.lb.common.to.mq.StockLockedTo;
import com.lb.common.utils.R;
import com.lb.gulimail.ware.entity.WareOrderTaskDetailEntity;
import com.lb.gulimail.ware.entity.WareOrderTaskEntity;
import com.lb.gulimail.ware.exception.NoStockException;
import com.lb.gulimail.ware.feign.MemberFeignService;
import com.lb.gulimail.ware.feign.OrderFeignService;
import com.lb.gulimail.ware.feign.ProductFeignService;
import com.lb.gulimail.ware.service.WareOrderTaskDetailService;
import com.lb.gulimail.ware.service.WareOrderTaskService;
import com.lb.gulimail.ware.dao.WareSkuDao;
import com.lb.gulimail.ware.entity.WareSkuEntity;
import com.lb.gulimail.ware.vo.*;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.common.utils.PageUtils;
import com.lb.common.utils.Query;

import com.lb.gulimail.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    private static final String DELAY_QUEUE_NAME="stock.delay.queue";//延迟队列名
    private static final String RELEASE_QUEUE_NAME="stock.release.stock.queue";//死信队列名
    private static final String STOCK_EXCHANGE_NAME="stock-event-exchange";//交换机名
    private static final String RELEASE_KEY="stock.release";//死信队列路由
    private static final String DELAY_KEY="stock.locked";//延迟队列路由


    @Autowired
    RabbitTemplate rabbitTemplate;
    @Resource
    WareSkuDao wareSkuDao;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    WareOrderTaskDetailService taskDetailService;
    @Autowired
    WareOrderTaskService taskService;
    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }


            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> vos = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }

    /**
     * 根据收货地址计算运费
     *
     * @param addrId
     * @return
     */
    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.info(addrId);
        MemberAddressVo vo = r.get("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        if (vo != null) {
            String phone = vo.getPhone();
            String fare = phone.substring(phone.length() - 1, phone.length());
            fareVo.setAddressVo(vo);
            fareVo.setFare(new BigDecimal(fare));
            return fareVo;
        }
        return null;
    }

    /**
     * 为订单锁定库存
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //保存库存工作单
        WareOrderTaskEntity orderTaskEntity = new WareOrderTaskEntity();
        orderTaskEntity.setOrderSn(vo.getOrderSn());
        taskService.save(orderTaskEntity);
        //查询每个商品在哪个仓库有库存
        List<OrderItemsVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());
            List<Long> wareIds = wareSkuDao.listWareIdHasStock(skuId);
            skuWareHasStock.setWareIds(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());
        //锁库存
        for (SkuWareHasStock wareHasStock : collect) {
            Boolean skuLock=false;
            Long skuId = wareHasStock.getSkuId();
            List<Long> wareIds = wareHasStock.getWareIds();
            if (wareIds == null && wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                Long count=wareSkuDao.lockSkuStock(skuId,wareId,wareHasStock.getNum());
                if (count==1){
                    //库存锁定成功,保存商品的锁定工作日志并发送给mq
                    skuLock=true;
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null,wareHasStock.getSkuId(),null,wareHasStock.getNum(),orderTaskEntity.getId(),wareId,1);
                    taskDetailService.save(taskDetailEntity);
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity,detailTo);
                    StockLockedTo stockLockedTo = new StockLockedTo(orderTaskEntity.getId(),detailTo);
                    //将工作单发送到mq中
                    rabbitTemplate.convertAndSend(STOCK_EXCHANGE_NAME,DELAY_KEY,stockLockedTo);
                    break;
                }else {
                    //锁失败寻找其他仓库是否有库存
                }
            }
            if (skuLock==false){
                //当前商品所有仓库都没货
                throw new NoStockException(skuId);
            }
        }

        //所有商品锁定库存
        return true;
    }

    @Transactional
    @Override
    public void unLockStock(StockLockedTo stockLockedTo) {
            System.out.println("收到解锁库存消息");
            StockDetailTo detail = stockLockedTo.getDetail();
            Long detailId = detail.getId();
            //1.先查询数据库是否有锁定库存记录，如果有则库存锁定成功，然后结合订单情况来判断是否解锁，如果没有则无需处理
            WareOrderTaskDetailEntity detailEntity = taskDetailService.getById(detailId);
            if (detailEntity!=null){
                //解锁-查询锁库存记录得到订单号，通过订单号获取订单状态
                Long taskId = stockLockedTo.getTaskId();
                WareOrderTaskEntity taskEntity = taskService.getById(taskId);
                R r = orderFeignService.getOrderStatus(taskEntity.getOrderSn());
                if (r.getCode()==0){
                    OrderEntityVo orderEntityVo = r.getData(new TypeReference<OrderEntityVo>() {
                    });
                    //订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】
                    if (orderEntityVo==null||orderEntityVo.getStatus()== OrderStatusEnum.CANCLED.getCode()){
                        if (detailEntity.getLockStatus()==1){
                            //订单取消或不存在，进行库存解锁
                            wareSkuDao.unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum());
                            //库存解锁后修改库存记录状态
                            WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
                            taskDetailEntity.setId(detailId);
                            taskDetailEntity.setLockStatus(2);
                            taskDetailService.updateById(taskDetailEntity);
                        }
                    }else if(orderEntityVo.getStatus()==OrderStatusEnum.RECIEVED.getCode()||orderEntityVo.getStatus()==OrderStatusEnum.PAYED.getCode()){
                        System.out.println("订单已经付款或者订单已经完成");
                    }else {
                        throw new RuntimeException("订单未关闭");
                    }
                }else {
                    throw new RuntimeException("远程服务失败");
                }
            }else {
                //库存锁定记录不存在，无需解锁
            }
    }

    /**
     * 防止订单服务卡顿，导致订单状态改不了，库存消息却优先到期
     * @param orderEntityTo
     */
    @Transactional
    @Override
    public void unLockStock(OrderEntityTo orderEntityTo) {
        String orderSn = orderEntityTo.getOrderSn();
        //查询最新的库存状态
        WareOrderTaskEntity taskEntity = taskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        Long taskId = taskEntity.getId();
        //按照工作单查到所有没有解锁的库存进行解锁
        List<WareOrderTaskDetailEntity> detailEntityList = taskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", taskId)
                .eq("lock_status",1));
        for (WareOrderTaskDetailEntity detail : detailEntityList) {
            //进行库存解锁
            wareSkuDao.unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum());
            //库存解锁后修改库存记录状态
            WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
            taskDetailEntity.setId(detail.getId());
            taskDetailEntity.setLockStatus(2);
            taskDetailService.updateById(taskDetailEntity);
        }

    }

    @Data
    class SkuWareHasStock {
        private Integer num;
        private Long skuId;
        private List<Long> wareIds;
    }
}