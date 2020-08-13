package com.lb.gulimail.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认页需要的数据
 */

public class OrderConfirmVo {
    //订单令牌防止重复提交
    private String orderToken;
    //商品件数
    Integer count;
    //收获地址
    private List<MemberAddressVo> addressVos;
    //所有选中的商品信息
    private List<OrderItemsVo> orderItemsVos;
    //会员优惠信息
    private Integer integration;
    //是否拥有库存
    @Getter@Setter
    Map<Long,Boolean> stock;
    public String getOrderToken() {
        return orderToken;
    }

    public void setOrderToken(String orderToken) {
        this.orderToken = orderToken;
    }

    public Integer getCount() {
        if (orderItemsVos!=null){
            return orderItemsVos.size();
        }else {
            return 0;
        }
    }

    public void setCount(Integer voCount) {
        this.count = voCount;
    }

    public List<MemberAddressVo> getAddressVos() {
        return addressVos;
    }

    public void setAddressVos(List<MemberAddressVo> addressVos) {
        this.addressVos = addressVos;
    }

    public List<OrderItemsVo> getOrderItemsVos() {
        return orderItemsVos;
    }

    public void setOrderItemsVos(List<OrderItemsVo> orderItemsVos) {
        this.orderItemsVos = orderItemsVos;
    }

    public Integer getIntegration() {
        return integration;
    }

    public void setIntegration(Integer integration) {
        this.integration = integration;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal sum=new BigDecimal("0");
        if (orderItemsVos!=null){
            for (OrderItemsVo itemsVo : orderItemsVos) {
                BigDecimal bigDecimal = itemsVo.getTotalPrice();
                sum=sum.add(bigDecimal);
            }
        }
        return sum;
    }


    public BigDecimal getPayPrice() {
        return getTotalPrice();
    }

}
