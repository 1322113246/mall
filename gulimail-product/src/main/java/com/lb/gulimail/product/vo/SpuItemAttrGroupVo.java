package com.lb.gulimail.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;
@Data
@ToString
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
