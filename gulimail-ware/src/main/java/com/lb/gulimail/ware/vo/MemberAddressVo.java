package com.lb.gulimail.ware.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


public class MemberAddressVo {
    @Getter@Setter
    private Long id;
    /**
     * member_id
     */
    @Getter@Setter
    private Long memberId;
    /**
     * 收货人姓名
     */
    @Getter@Setter
    private String name;
    /**
     * 电话
     */
    @Getter@Setter
    private String phone;
    /**
     * 邮政编码
     */
    @Getter@Setter
    private String postCode;
    /**
     * 省份/直辖市
     */
    @Getter@Setter
    private String province;
    /**
     * 城市
     */
    @Getter@Setter
    private String city;
    /**
     * 区
     */
    @Getter@Setter
    private String region;
    /**
     * 详细地址(街道)
     */
    @Getter@Setter
    private String detailAddress;
    /**
     * 省市区代码
     */
    @Getter@Setter
    private String areacode;
    /**
     * 是否默认
     */
    @Getter@Setter
    private Integer defaultStatus;
    /**
     * 具体地址
     */
    private String address;

    public String getAddress() {
        return province+city+region+detailAddress;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
