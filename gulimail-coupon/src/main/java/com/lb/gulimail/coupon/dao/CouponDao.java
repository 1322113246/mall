package com.lb.gulimail.coupon.dao;

import com.lb.gulimail.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 *
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
