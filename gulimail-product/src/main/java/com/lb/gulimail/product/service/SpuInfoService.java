package com.lb.gulimail.product.service;

import com.lb.gulimail.product.vo.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.common.utils.PageUtils;
import com.lb.gulimail.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 * * @author liangbin
 *  * @email 1322113246@qq.com
 *  * @date 2020-07-06 15:11:19
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity infoEntity);


    PageUtils queryPageByCondition(Map<String, Object> params);


    void up(Long spuId);

    SpuInfoEntity getSpuBySkuId(Long skuId);
}

