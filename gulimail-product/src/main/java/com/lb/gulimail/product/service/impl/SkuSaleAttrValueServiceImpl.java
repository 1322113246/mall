package com.lb.gulimail.product.service.impl;

import com.lb.gulimail.product.vo.SkuItemSaleAttrVo;
import com.lb.gulimail.product.dao.SkuSaleAttrValueDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.common.utils.PageUtils;
import com.lb.common.utils.Query;

import com.lb.gulimail.product.entity.SkuSaleAttrValueEntity;
import com.lb.gulimail.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId) {
        SkuSaleAttrValueDao baseMapper = this.getBaseMapper();
        List<SkuItemSaleAttrVo> skuItemSaleAttrVos=baseMapper.getSaleAttrsBySpuId(spuId);
        return skuItemSaleAttrVos;
    }

    @Override
    public List<String> getSaleAttrWithStringList(Long skuId) {
        SkuSaleAttrValueDao saleAttrValueDao = this.baseMapper;
        return saleAttrValueDao.getSaleAttrWithStringList(skuId);
    }

}