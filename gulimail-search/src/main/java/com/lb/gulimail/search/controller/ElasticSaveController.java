package com.lb.gulimail.search.controller;

import com.lb.common.exception.BizCodeEnume;
import com.lb.common.to.es.SkuEsModel;
import com.lb.common.utils.R;
import com.lb.gulimail.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController {
    @Autowired
    ProductSaveService productSaveService;
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
        boolean b=false;
        try {
            b=productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("es商品上架错误:{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION);
        }
        if (b){
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION);
        }
        return R.ok();
    }
}
