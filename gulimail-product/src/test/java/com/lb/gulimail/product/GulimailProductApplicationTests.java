package com.lb.gulimail.product;


import com.lb.gulimail.product.entity.BrandEntity;
import com.lb.gulimail.product.feign.WareFeignService;
import com.atguigu.gulimail.product.service.*;

import com.lb.gulimail.product.service.*;
import com.lb.gulimail.product.vo.SkuItemSaleAttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimailProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient client;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Test
    public void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setDescript("test");
//        brandEntity.setName("test");
//        brandService.save(brandEntity);
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1l));
        list.forEach((item) -> {
            System.out.println(item.getName());
        });
    }

    @Test

    public void test(){
        List<SkuItemSaleAttrVo> saleAttrBySpuId = skuSaleAttrValueService.getSaleAttrBySpuId(20l);
        System.out.println(saleAttrBySpuId);
    }

}
