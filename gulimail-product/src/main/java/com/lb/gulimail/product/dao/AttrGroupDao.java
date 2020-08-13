package com.lb.gulimail.product.dao;

import com.lb.gulimail.product.entity.AttrGroupEntity;
import com.lb.gulimail.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-07-06 15:11:19
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

     List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);

}
