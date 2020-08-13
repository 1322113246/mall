package com.lb.gulimail.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.common.utils.PageUtils;
import com.lb.gulimail.coupon.entity.CouponSpuRelationEntity;

import java.util.Map;

/**
 * 优惠券与产品关联
 *
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

