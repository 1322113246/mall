package com.lb.gulimail.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.common.utils.PageUtils;
import com.lb.gulimail.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-07-06 15:11:19
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveProductAttr(List<ProductAttrValueEntity> collect);


    List<ProductAttrValueEntity> baseAttrlistforspu(Long spuId);


    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities);


}

