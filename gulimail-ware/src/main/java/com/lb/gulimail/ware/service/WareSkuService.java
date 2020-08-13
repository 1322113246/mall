package com.lb.gulimail.ware.service;

import com.lb.common.to.OrderEntityTo;
import com.lb.common.to.mq.StockLockedTo;
import com.lb.gulimail.ware.vo.FareVo;
import com.lb.gulimail.ware.vo.SkuHasStockVo;
import com.lb.gulimail.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.common.utils.PageUtils;
import com.lb.gulimail.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-07-06 15:11:19
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);


    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    FareVo getFare(Long addrId);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo stockLockedTo);

    void unLockStock(OrderEntityTo orderEntityTo);
}

