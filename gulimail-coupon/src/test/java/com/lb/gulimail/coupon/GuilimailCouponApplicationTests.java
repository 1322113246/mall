package com.lb.gulimail.coupon;

import com.lb.gulimail.coupon.service.CouponService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GuilimailCouponApplicationTests {
    @Autowired
    CouponService couponService;

    @Test
    void contextLoads() {

    }

}
