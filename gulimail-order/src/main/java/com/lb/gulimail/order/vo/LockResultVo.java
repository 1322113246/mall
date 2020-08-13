package com.lb.gulimail.order.vo;

import lombok.Data;

@Data
public class LockResultVo {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
