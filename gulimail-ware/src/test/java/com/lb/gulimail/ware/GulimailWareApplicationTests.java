package com.lb.gulimail.ware;


import com.lb.gulimail.ware.entity.WareInfoEntity;
import com.lb.gulimail.ware.service.WareInfoService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimailWareApplicationTests {
    @Autowired
    WareInfoService wareInfoService;

    @Test
    void contextLoads() {
        WareInfoEntity wareInfoEntity = new WareInfoEntity();
        wareInfoEntity.setName("test");
        boolean save = wareInfoService.save(wareInfoEntity);
    }

}
