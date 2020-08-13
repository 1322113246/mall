package com.lb.gulimail.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.lb.common.exception.BizCodeEnume;
import com.lb.gulimail.ware.exception.NoStockException;
import com.lb.gulimail.ware.vo.FareVo;
import com.lb.gulimail.ware.vo.SkuHasStockVo;
import com.lb.gulimail.ware.vo.WareSkuLockVo;
import com.lb.gulimail.ware.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lb.gulimail.ware.service.WareSkuService;
import com.lb.common.utils.PageUtils;
import com.lb.common.utils.R;



/**
 * 商品库存
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-07-06 15:11:19
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;
    /**
     * 锁库存
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo){
        try {
            Boolean lockStock = wareSkuService.orderLockStock(vo);
        }catch (NoStockException e){
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION);
        }
        return R.ok();
    }
    /**
     *获取运费信息
     */
    @GetMapping("/fare")
    public R getFare(@RequestParam("addrId") Long addrId){
        FareVo fare = wareSkuService.getFare(addrId);
        return R.ok().setData(fare);
    }
    /**
     * 查询sku是否有库存
     */
    @PostMapping("/hasstock")
    public  R getSkusHasStock(@RequestBody List<Long> skuIds){
        List<SkuHasStockVo> vos=wareSkuService.getSkuHasStock(skuIds);

        return R.ok().setData(vos);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
