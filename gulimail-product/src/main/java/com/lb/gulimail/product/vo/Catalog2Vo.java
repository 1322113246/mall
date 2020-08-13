package com.lb.gulimail.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
//二级分类
public class Catalog2Vo {
    private String catalog1Id;//一级父分类Id
    private List<Catalog3Vo> catalog3List;//三级子分类
    private String id;
    private String name;

    //三级分类
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catalog3Vo{
        private String catalog2Id;//二级父分类id
        private String id;
        private String name;
    }
}
