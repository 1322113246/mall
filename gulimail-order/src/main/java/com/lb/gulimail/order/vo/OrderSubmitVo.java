package com.lb.gulimail.order.vo;

import lombok.Data;
import lombok.ToString;
import sun.rmi.runtime.Log;

import java.math.BigDecimal;

/**
 * 封装订单提交数据
 */
@Data
@ToString
public class OrderSubmitVo {
    private Long addrId;//收获地址id
    private Integer payType;//支付方式
    //勾选的购物车内的商品

    private String orderToken;//防重令牌

    private BigDecimal payPrice;//应付价格 验价

    private  String note;//订单备注
}
