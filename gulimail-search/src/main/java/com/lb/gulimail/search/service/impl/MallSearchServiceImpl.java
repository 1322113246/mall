package com.lb.gulimail.search.service.impl;
import com.alibaba.fastjson.JSON;
import com.lb.common.to.es.SkuEsModel;
import com.lb.gulimail.search.config.GulimailEsConfig;
import com.lb.gulimail.search.constant.EsConstant;
import com.lb.gulimail.search.service.MallSearchService;
import com.lb.gulimail.search.vo.SearchParam;
import com.lb.gulimail.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient client;
    @Override
    public SearchResult search(SearchParam param) {
        //1.动态构建出查询需要的DSL语句
        SearchResult result=null;
        //2.准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            //3.执行检索请求
            SearchResponse response = client.search(searchRequest, GulimailEsConfig.COMMON_OPTIONS);
            //4.将返回数据封装成指定格式
            result=getSearchResult(response,param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 准备检索请求
     * #模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1.构建bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //2.模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
        //2.1must模糊匹配
         if (!StringUtils.isEmpty(param.getKeyword())){
             boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
         }
         //2.2bool-filter
        //2.2.1按照三级分类id查询
        if (param.getCatalog3Id()!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        //2.2.2按照品牌id查询
        if (param.getBrandId()!=null&&param.getBrandId().size()>0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }
        //2.2.3按照指定属性查询
        if (param.getAttrs()!=null&&param.getAttrs().size()>0){
            for (String attrStr: param.getAttrs()) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId=s[0];//检索的属性id
                String[] attrValue=s[1].split(":");//属性检索的值
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValue));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);

                boolQueryBuilder.filter(nestedQuery);
            }

        }
        //2.2.4按照是否拥有库存查询
        if (param.getHasStock()!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",param.getHasStock()==1));
        }
        //2.2.5按照价格区间检索
        if (!StringUtils.isEmpty(param.getSkuPrice())){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] split = param.getSkuPrice().split("_");
            if (split.length==2){
                rangeQuery.gte(split[0]).lte(split[1]);
            }else if (split.length==1){
                if (param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(split[0]);
                }
                if (param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(split[0]);
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        //把查询条件封装好
        sourceBuilder.query(boolQueryBuilder);
        //2.3排序，分页，高亮
        //2.3.1 排序hotScore_desc
        if (!StringUtils.isEmpty(param.getSort())){
            String[] split = param.getSort().split("_");
            SortOrder sortOrder=split[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(split[0],sortOrder);
        }
        //2.3.2 分页
        sourceBuilder.from((param.getPageNum()-1)* EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //2.3.3高亮
        if (!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        //2.4聚合分析

        //2.4.1品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);
        //2.4.2分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(50);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);
        //2.4.3 属性集合
        NestedAggregationBuilder attrs_agg = AggregationBuilders.nested("attrs_agg","attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrs_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attrs_agg);

        System.out.println(sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * 将返回数据封装成指定格式SearchResult对象
     * @return
     */
    private SearchResult getSearchResult(SearchResponse response,SearchParam param) {
        SearchResult result = new SearchResult();
        //返回查询到的商品
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<SkuEsModel> esModels=new ArrayList<>();
        if (searchHits!=null&&searchHits.length>0){
            for (SearchHit hit:searchHits){
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);


        //返回属性信息
        List<SearchResult.AttrVo> attrVos=new ArrayList<>();
        ParsedNested attrs_agg = response.getAggregations().get("attrs_agg");
        ParsedLongTerms attr_id_agg = attrs_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //得到属性id
            String keyAsString = bucket.getKeyAsString();
            long attrId = Long.parseLong(keyAsString);
            attrVo.setAttrId(attrId);
            //得到属性名字
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            //得到属性的值
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(item -> {
                String attrValue = item.getKeyAsString();
                return attrValue;
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrVos(attrVos);

        //返回分类信息
        List<SearchResult.CatalogVo> catalogVos=new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //得到分类名字
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName= catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogVos(catalogVos);


        //返回品牌信息
        List<SearchResult.BrandVo> brandVos=new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            new SearchResult.BrandVo();
            //得到品牌的id
            String keyAsString = bucket.getKeyAsString();
            long brandId = Long.parseLong(keyAsString);
            brandVo.setBrandId(brandId);
            //得到品牌的名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            //得到品牌的图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrandVos(brandVos);

        //返回当前页码
        result.setPageNum(param.getPageNum());

        //返回总数量
        long totalHits = hits.getTotalHits().value;
        result.setTotal(totalHits);

        //返回总页码数
        Integer totalPage=(int) totalHits % EsConstant.PRODUCT_PAGESIZE==0?(int)totalHits/EsConstant.PRODUCT_PAGESIZE:(int)(totalHits/EsConstant.PRODUCT_PAGESIZE+1);
        result.setTotalPages(totalPage);
        return result;
    }

}
