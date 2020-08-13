package com.lb.gulimail.order.vo;

import com.lb.gulimail.order.entity.OrderEntity;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SubmitOrderRespVo {
    private OrderEntity order;//订单信息
    private Integer code;//0成功 1失败 2验价不相等
}
