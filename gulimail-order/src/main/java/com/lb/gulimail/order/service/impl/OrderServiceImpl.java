package com.lb.gulimail.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lb.common.exception.NoStockException;
import com.lb.common.to.OrderEntityTo;
import com.lb.common.utils.R;
import com.lb.common.vo.MemberRespVo;
import com.lb.gulimail.order.constant.OrderConstant;
import com.lb.gulimail.order.entity.OrderItemEntity;
import com.lb.gulimail.order.entity.PaymentInfoEntity;
import com.lb.gulimail.order.enume.OrderStatusEnum;
import com.lb.gulimail.order.feign.CartFeignService;
import com.lb.gulimail.order.feign.MemberFeignService;
import com.lb.gulimail.order.feign.ProductFeignService;
import com.lb.gulimail.order.feign.WareFeignService;
import com.lb.gulimail.order.interceptor.OrderInterceptor;
import com.lb.gulimail.order.service.OrderItemService;
import com.lb.gulimail.order.service.PaymentInfoService;
import com.lb.gulimail.order.to.OrderCreateTo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lb.gulimail.order.dao.OrderDao;
import com.lb.gulimail.order.entity.OrderEntity;
import com.lb.gulimail.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.common.utils.PageUtils;
import com.lb.common.utils.Query;

import com.lb.gulimail.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        MemberRespVo memberRespVo = OrderInterceptor.threadLocal.get();
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //收获地址列表
        CompletableFuture<Void> addrFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addr = memberFeignService.getAddr(memberRespVo.getId());
            confirmVo.setAddressVos(addr);

        },threadPoolExecutor);
        //优惠卷
        CompletableFuture<Void> interationFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            confirmVo.setIntegration(memberRespVo.getIntegration());
        }, threadPoolExecutor);
        //所有选中购物项
        CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemsVo> currentUserCartItem = cartFeignService.getCurrentUserCartItem();
            confirmVo.setOrderItemsVos(currentUserCartItem);
        }, threadPoolExecutor).thenRunAsync(()->{
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemsVo> orderItemsVos = confirmVo.getOrderItemsVos();
            List<Long> skuIds = orderItemsVos.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R r = wareFeignService.getSkusHasStock(skuIds);
            List<SkuHasStockVo> skuHasStockVos = r.getData(new TypeReference<List<SkuHasStockVo>>() {});
            if (skuHasStockVos!=null){
                Map<Long, Boolean> map = skuHasStockVos.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                confirmVo.setStock(map);
            }
        },threadPoolExecutor);
        //防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(addrFuture,interationFuture,orderItemFuture).get();
        return confirmVo;
    }

    /**
     * 下单流程:
     * 1.验证令牌 2.生成订单 3.库存锁定
     * @param vo
     * @return
     */
//    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderRespVo respVo = new SubmitOrderRespVo();
        respVo.setCode(0);
        MemberRespVo memberRespVo = OrderInterceptor.threadLocal.get();
        //1.验证令牌
        String orderToken = vo.getOrderToken();
        //验证redis中令牌的lua脚本
        String delTokenLua="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del' ,KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(delTokenLua, Long.class);
        Long resultCode = redisTemplate.execute(script, Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),orderToken);
        if (resultCode==1){
            //令牌验证成功
            //1.创建订单收货、价格和商品信息;
            OrderCreateTo order= createOrder(vo);
            //2.验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01){
                //3.保存订单
                saveOrder(order);
                //4.库存锁定
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemsVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemsVo itemsVo = new OrderItemsVo();
                    itemsVo.setSkuId(item.getSkuId());
                    itemsVo.setCount(item.getSkuQuantity());
                    itemsVo.setTitle(item.getSkuName());
                    return itemsVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(locks);
                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode()==0){
                    //锁成功了
                    respVo.setCode(0);
                    respVo.setOrder(order.getOrder());
                    //订单创建成功,发送消息到mq中
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
                    return respVo;
                }else {
                    //锁失败了
                    throw new NoStockException(0l);
                }
            }else{
                //价格不一致
                respVo.setCode(2);
                return respVo;
            }
        }else {
            //令牌验证失败
            respVo.setCode(1);
            return respVo;
        }
    }

    /**
     * 关闭订单
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        OrderEntity byId = this.getById(orderEntity.getId());
        if (byId!=null&&byId.getStatus()== OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity orderEntity1 = new OrderEntity();
            orderEntity1.setId(orderEntity.getId());
            orderEntity1.setStatus(OrderStatusEnum.CANCLED.getCode());
            //关闭订单状态
            this.updateById(orderEntity1);
            //关单成功，发送解锁库存消息到mq
            OrderEntityTo orderEntityTo = new OrderEntityTo();
            BeanUtils.copyProperties(byId,orderEntityTo);
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other.#",orderEntityTo);
        }
    }

    /**
     * 获取订单支付信息
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        PayVo payVo = new PayVo();
        BigDecimal bigDecimal = orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(bigDecimal.toString());
        payVo.setOut_trade_no(orderEntity.getOrderSn());
        payVo.setSubject("测试");
        payVo.setBody("测试");
        return payVo;
    }

    /**
     * 查询订单以及其商品信息进行分页
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = OrderInterceptor.threadLocal.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id",memberRespVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> orderEntityList = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> items = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItems(items);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(orderEntityList);
        return new PageUtils(page);
    }

    /**
     * 处理支付宝的支付结果
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        try {
            //1.保存交易流水
            PaymentInfoEntity entity = new PaymentInfoEntity();
            entity.setAlipayTradeNo(vo.getTrade_no());
            entity.setOrderSn(vo.getOut_trade_no());
            entity.setPaymentStatus(vo.getTrade_status());
            entity.setCallbackTime(vo.getNotify_time());
            entity.setSubject(vo.getSubject());
            paymentInfoService.save(entity);
            //2.修改订单状态信息
            if(vo.getTrade_status().equals("TRADE_SUCCESS")||vo.getTrade_status().equals("TRADE_FINISHED")){
                //支付成功
                String orderSn = vo.getOut_trade_no();
                this.baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
            }
            return "success";
        }catch (Exception e){
            System.out.println("错误："+e.getMessage());;
            return "false";
        }
    }

    /**
     * 保存订单所有相关数据
     * @param order
     */
    @Transactional
    public void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        baseMapper.insert(orderEntity);
        List<OrderItemEntity> orderItems = buildOrderItems(order.getOrder().getOrderSn());
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单收货、价格和商品信息;
     * @param vo
     * @return
     */
    private OrderCreateTo createOrder(OrderSubmitVo vo){
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //订单信息主要包括收货信息等
        OrderEntity orderEntity = buildOrderEntity(vo);
        //订单商品数据
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderEntity.getOrderSn());//生成订单商品数据
        //价格信息
        computePrice(orderEntity,orderItemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);
        return orderCreateTo;
    }

    /**
     * 价格优惠信息
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal("0.00");
        BigDecimal couponAmount= new BigDecimal("0.00");
        BigDecimal integrationAmount= new BigDecimal("0.00");
        BigDecimal promotionAmount = new BigDecimal("0.00");
        Integer integration=0;
        Integer growth=0;
        for (OrderItemEntity itemEntity : orderItemEntities) {
            couponAmount = itemEntity.getCouponAmount();
            integrationAmount = itemEntity.getIntegrationAmount();
            promotionAmount = itemEntity.getPromotionAmount();
            total = total.add(itemEntity.getRealAmount());
            integration+=itemEntity.getGiftIntegration();
            growth+=itemEntity.getGiftGrowth();
        }
        orderEntity.setIntegration(integration);
        orderEntity.setGrowth(growth);
        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setCouponAmount(couponAmount);
    }

    /**
     * 构建所有订单商品数据
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn){
        //再次确定购物车内选中的商品
        List<OrderItemsVo> cartItem = cartFeignService.getCurrentUserCartItem();
        if (cartItem!=null&&cartItem.size()>0){
            List<OrderItemEntity> orderItemEntityList = cartItem.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItemEntityList;
        }
        return null;
    }
    /**
     * 构建某个订单商品数据
     */
    private OrderItemEntity buildOrderItem(OrderItemsVo vo){
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //sku信息
        orderItemEntity.setSkuId(vo.getSkuId());
        orderItemEntity.setSkuName(vo.getTitle());
        orderItemEntity.setSkuPic(vo.getImage());
        orderItemEntity.setSkuPrice(vo.getTotalPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(vo.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(vo.getCount());
        //积分信息
        orderItemEntity.setGiftGrowth(vo.getPrice().multiply(new BigDecimal(vo.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(vo.getPrice().multiply(new BigDecimal(vo.getCount().toString())).intValue());
        //spu信息
        SpuInfoVo spuInfoVo = productFeignService.getSpuBySkuId(vo.getSkuId()).getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        //价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单商品的实际金额,总额-优惠
        BigDecimal originPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal realAmount = originPrice.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realAmount);
        return orderItemEntity;
    }
    /**
     * 构建订单收货信息
     * @param vo
     * @return
     */
    private OrderEntity buildOrderEntity(OrderSubmitVo vo){
        MemberRespVo memberRespVo = OrderInterceptor.threadLocal.get();
        OrderEntity orderEntity = new OrderEntity();
        //会员信息
        orderEntity.setMemberId(memberRespVo.getId());
        orderEntity.setMemberUsername(memberRespVo.getUsername());
        //设置订单号
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setDeleteStatus(0);//0表示未删除
        //设置地址信息和邮费
        FareVo  fareVo = wareFeignService.getFare(vo.getAddrId()).getData(new TypeReference<FareVo>(){
        });
        MemberAddressVo addressVo = fareVo.getAddressVo();//地址信息
        BigDecimal fare = fareVo.getFare();
        orderEntity.setFreightAmount(fare);//运费金额
        orderEntity.setReceiverCity(addressVo.getCity());//市
        orderEntity.setReceiverDetailAddress(addressVo.getDetailAddress());//街道
        orderEntity.setReceiverProvince(addressVo.getProvince());//省份
        orderEntity.setReceiverRegion(addressVo.getRegion());//区
        orderEntity.setReceiverName(addressVo.getName());//收货人
        orderEntity.setReceiverPhone(addressVo.getPhone());//收获人手机
        orderEntity.setReceiverPostCode(addressVo.getPostCode());//邮政编号
        //设置订单状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        return orderEntity;
    }
}