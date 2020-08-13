package com.lb.gulimail.product.web;

import com.lb.gulimail.product.service.SkuInfoService;
import com.lb.gulimail.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {
    @Autowired
    SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model){

        SkuItemVo itemVo=skuInfoService.item(skuId);
        model.addAttribute("item",itemVo);
        return "item";
    }
}
