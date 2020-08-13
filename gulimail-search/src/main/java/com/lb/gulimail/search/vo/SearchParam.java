package com.lb.gulimail.search.vo;

import lombok.Data;

import java.util.List;


@Data
public class SearchParam {
    private String keyword;//全文匹配关键字
    private Long catalog3Id;//三级分类Id
    private String sort;//排序 sale=saleCount_asc或者saleCount_desc
    private Integer hasStock;//是否只显示有货 0无/1有
    private String skuPrice;//价格范围查询 1_500相当于1~500之间
    private List<Long> brandId;//品牌Id,安装品牌查询
    private List<String> attrs;//按属性查询 attrs=1_5存:128g
    private Integer pageNum=1;//页码

}
