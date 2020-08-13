package com.lb.gulimail.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String orderSn;//订单号
    private List<OrderItemsVo> locks;//需要锁住的库存信息

}
