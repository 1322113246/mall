package com.lb.gulimail.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;
@Data
@ToString
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuId> attrValues;
}
