package com.lb.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lb.common.constant.CartConstant;
import com.lb.gulimall.cart.feign.ProductFeignService;
import com.lb.gulimall.cart.interceptor.CartInterceptor;
import com.lb.gulimall.cart.service.CartService;
import com.lb.gulimall.cart.vo.Cart;
import com.lb.gulimall.cart.vo.CartItem;
import com.lb.gulimall.cart.vo.SkuInfoVo;
import com.lb.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> ops = getCartItemOps();
        String o = (String) ops.get(skuId.toString());
        if (!StringUtils.isEmpty(o)){
            //若购物车内有商品,则修改数量
            CartItem cartItem = JSON.parseObject(o, CartItem.class);
            Integer count = cartItem.getCount();
            cartItem.setCount(count+num);
            String jsonString = JSON.toJSONString(cartItem);
            ops.put(skuId.toString(),jsonString);
            return cartItem;
        }
        //添加新商品
        CartItem cartItem = new CartItem();
        //远程查询商品基本信息
        CompletableFuture<Void> skuInfoF = CompletableFuture.runAsync(() -> {
            SkuInfoVo skuInfoVo = productFeignService.info(skuId).get("skuInfo", new TypeReference<SkuInfoVo>() {
            });
            //将商品添加到购物车
            cartItem.setSkuId(skuInfoVo.getSkuId());
            cartItem.setCheck(true);
            cartItem.setCount(num);
            cartItem.setImage(skuInfoVo.getSkuDefaultImg());
            cartItem.setTitle(skuInfoVo.getSkuTitle());
            cartItem.setPrice(skuInfoVo.getPrice());
        }, executor);
        //远程查询商品的销售属性
        CompletableFuture<Void> attrValueF = CompletableFuture.runAsync(() -> {
            List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
            cartItem.setSkuAttr(attrValues);
        });
        CompletableFuture.allOf(skuInfoF, attrValueF).get();
        String jsonString = JSON.toJSONString(cartItem);
        ops.put(skuId.toString(),jsonString);
        return cartItem;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartItemOps();
        String o = (String) ops.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(o, CartItem.class);
        return cartItem;
    }

    /**
     * 获取购物车中所有商品信息
     * @return
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()!=null){
            //登录了
            String cartKey=CartConstant.CART_PREFIX+userInfoTo.getUserId();
            String tempCartKey=CartConstant.CART_PREFIX+userInfoTo.getUserKey();
            BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
            //如果临时购物车的数据还没有合并
            //先查询临时购物车数据
            List<CartItem> tempCartItems = getCartItemsFromRedis(tempCartKey);
            if (tempCartItems!=null&&tempCartItems.size()>0){
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(),tempCartItem.getCount());
                }
                //合并后清除临时购物车的数据
                clearCart(tempCartKey);
            }
            //获取登录后的购物车数据
            List<CartItem> cartItems = getCartItemsFromRedis(cartKey);
            cart.setItems(cartItems);
        }else{
            //未登录
            String cartKey = CartConstant.CART_PREFIX+userInfoTo.getUserKey();
            List<CartItem> cartItemList = getCartItemsFromRedis(cartKey);
            cart.setItems(cartItemList);
        }
        return cart;
    }
    @Override
    public void clearCart(String cartKey){
         stringRedisTemplate.delete(cartKey);
    }

    /**
     * 改变商品的选中状态
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1?true:false);
        String jsonString = JSON.toJSONString(cartItem);
        BoundHashOperations<String, Object, Object> cartItemOps = getCartItemOps();
        cartItemOps.put(skuId.toString(),jsonString);
    }

    @Override
    public void changeCartItemNum(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String jsonString = JSON.toJSONString(cartItem);
        BoundHashOperations<String, Object, Object> ops = getCartItemOps();
        ops.put(skuId.toString(),jsonString);
    }

    @Override
    public void deleteOne(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartItemOps();
        ops.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCurrentUserCartItem() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()==null){
            return null;
        }
        List<CartItem> cartItems = getCartItemsFromRedis(CartConstant.CART_PREFIX + userInfoTo.getUserId());
        List<CartItem> cartItemList = cartItems.stream().filter(item -> item.getCheck()).map((item)->{
            //更新为最新价格
            BigDecimal currentPrice = productFeignService.getCurrentPrice(item.getSkuId());
            item.setPrice(currentPrice);
            return item;
        }).collect(Collectors.toList());

        return cartItemList;
    }

    /**
     * 获取购物车数据的工具方法
     * @param cartKey
     * @return
     */
    public List<CartItem> getCartItemsFromRedis(String cartKey){
        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        if(values!=null&&values.size()>0){
            List<CartItem> cartItemList = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return cartItemList;
        }
        return null;
    }
    private BoundHashOperations<String, Object, Object> getCartItemOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //设置存入购物车的key值
        String cartKey="";
        if (userInfoTo.getUserId()!=null){
            cartKey= CartConstant.CART_PREFIX+userInfoTo.getUserId();
        }else{
            cartKey=CartConstant.CART_PREFIX+userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(cartKey);

        return ops;
    }
}
