package com.lb.gulimall.cart.feign;

import com.lb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("gulimail-product")
public interface ProductFeignService {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    /**
     * 获取当前最新价格
     * @param skuId
     * @return
     */
    @GetMapping("/product/skuinfo/currentPrice")
    BigDecimal getCurrentPrice(@RequestParam("skuId") Long skuId);
}
