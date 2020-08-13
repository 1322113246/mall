package com.lb.gulimall.cart.controller;

import com.lb.common.utils.R;
import com.lb.gulimall.cart.service.CartService;
import com.lb.gulimall.cart.vo.Cart;
import com.lb.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {
    @Autowired
    CartService cartService;
    @GetMapping("cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart=cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }
    /**
     *
     */
    @PostMapping("/checkItem")
    @ResponseBody
    public R checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return R.ok();
    }
    /**
     * 添加商品到页面
     * @return
     */
    @GetMapping("addToCart")
    public String addToCart(@RequestParam("skuId")Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId,num);
        redirectAttributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccessPage.html";
    }
    @GetMapping("/addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId")Long skuId,Model model){
       CartItem cartItem= cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }
    /**
     * 修改购物车中商品数量
     */
    @PostMapping("/changeCartItemNum")
    @ResponseBody
    public R changeCartItemNum(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num){
        cartService.changeCartItemNum(skuId,num);
        return R.ok();
    }
    /**
     * 删除购物车内商品
     */
    @PostMapping("/deleteCartItem")
    @ResponseBody
    public R deleteCartItem(@RequestParam("skuId") Long skuId){
        cartService.deleteOne(skuId);
        return R.ok();
    }
    /**
     * 获取当前用户选中的购物项
     */
    @GetMapping("/currentUserCartItem")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItem(){
        return cartService.getCurrentUserCartItem();
    }
}
