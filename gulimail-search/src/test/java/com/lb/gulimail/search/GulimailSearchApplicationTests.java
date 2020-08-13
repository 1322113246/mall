package com.lb.gulimail.search;


import com.alibaba.fastjson.JSON;
import com.lb.gulimail.search.config.GulimailEsConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimailSearchApplicationTests {
    @Autowired
    private RestHighLevelClient client;

    @Test
    /**
     * 按照年龄聚合，并计算每个年龄阶段的平均工资
     */
    public void testAggs() throws IOException {
        //创建检索请求
       SearchRequest searchRequest=new SearchRequest();
       //指定检索
       searchRequest.indices("bank");
       //设置检索条件
        SearchSourceBuilder builder=new SearchSourceBuilder();
        builder.query(QueryBuilders.matchAllQuery());
        //构造聚合检索条件
        TermsAggregationBuilder aggregations = AggregationBuilders.terms("by_age").field("age").size(10);
        aggregations.subAggregation(AggregationBuilders.avg("avg_balance").field("balance"));
        //将聚合检索条件添加到检索条件中
        builder.aggregation(aggregations);
        searchRequest.source(builder);
        //执行检索
        SearchResponse response = client.search(searchRequest, GulimailEsConfig.COMMON_OPTIONS);
        //输出检索
        Aggregations responseAggregations = response.getAggregations();
        List<Aggregation> aggregationList = responseAggregations.asList();
        System.out.println(aggregationList);

    }

    @Test
    public void indexApi() throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id("1");
        User user = new User();
        user.setAge(18);
        user.setGender("男");
        user.setUsername("张三");
        String jsonString = JSON.toJSONString(user);
        request.source(jsonString,XContentType.JSON);
        //执行保存
        IndexResponse index = client.index(request, GulimailEsConfig.COMMON_OPTIONS);
        //提取数据
        System.out.println(index);
    }
    @Data
    class User{
        private String username;
        private Integer age;
        private String gender;
    }
}
