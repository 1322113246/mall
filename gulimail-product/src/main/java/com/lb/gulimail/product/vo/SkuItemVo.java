package com.lb.gulimail.product.vo;

import com.lb.gulimail.product.entity.SkuImagesEntity;
import com.lb.gulimail.product.entity.SkuInfoEntity;
import com.lb.gulimail.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;
@Data
@ToString
public class SkuItemVo {
    //sku基本信息获取  pms_sku_info
    private SkuInfoEntity skuInfoEntity;
    private Boolean hasStock =true;
    //sku图片信息获取 pms_sku_images
    private List<SkuImagesEntity> images;
    //获取spu的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttr;
    //获取spu的介绍
    SpuInfoDescEntity desc;
    //获取spu的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;


}

