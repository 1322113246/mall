package com.lb.gulimail.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lb.common.to.es.SkuEsModel;
import com.lb.gulimail.search.config.GulimailEsConfig;
import com.lb.gulimail.search.constant.EsConstant;
import com.lb.gulimail.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl  implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        //保存到Es
        //1.建立product索引
        //2.保存数据到Es
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel: skuEsModels){
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(skuEsModel);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimailEsConfig.COMMON_OPTIONS);
        //TODO 如果批量错误
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        if (bulk.hasFailures()){
            log.error("商品上架异常：{}", collect);
        }else {
            log.info("商品上架完成：{},返回数据：{}",collect,bulk.toString());
        }
        return bulk.hasFailures();
    }
}
