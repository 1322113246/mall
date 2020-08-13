package com.lb.gulimail.order.dao;

import com.lb.gulimail.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-06-07 17:28:55
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {

}
