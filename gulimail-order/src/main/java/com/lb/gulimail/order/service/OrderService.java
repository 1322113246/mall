package com.lb.gulimail.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.common.utils.PageUtils;
import com.lb.gulimail.order.entity.OrderEntity;
import com.lb.gulimail.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-06-07 17:28:55
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;


    SubmitOrderRespVo submitOrder(OrderSubmitVo vo);

    void closeOrder(OrderEntity orderEntity);

    PayVo getOrderPay(String orderSn);

    /**
     * 查询订单以及其商品信息
     * @param params
     * @return
     */
    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);
}

