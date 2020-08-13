package com.lb.gulimail.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.common.utils.PageUtils;
import com.lb.gulimail.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-07-06 15:11:19
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(SpuInfoDescEntity descEntity);


}

