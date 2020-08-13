package com.lb.gulimail.order.to;

import com.lb.gulimail.order.entity.OrderEntity;
import com.lb.gulimail.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;//应付金额

    private BigDecimal fare;//运费
}
