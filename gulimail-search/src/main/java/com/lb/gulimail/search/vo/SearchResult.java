package com.lb.gulimail.search.vo;

import com.lb.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;


@Data
public class SearchResult {
    private List<SkuEsModel> products;//从es查询到的商品信息
    private Integer pageNum;//当前页面
    private Long total;//总记录
    private Integer totalPages;//总页码
    private List<CatalogVo> catalogVos;//当前查询到的所有分类
    private List<BrandVo> brandVos;//当前查询到的品牌信息
    private List<AttrVo> attrVos;//当前查询到的所有属性信息
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
