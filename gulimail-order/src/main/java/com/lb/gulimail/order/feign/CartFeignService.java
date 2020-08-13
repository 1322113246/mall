package com.lb.gulimail.order.feign;

import com.lb.gulimail.order.vo.OrderItemsVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService {
    /**
     * 获取当前用户选中的购物项
     */
    @GetMapping("/currentUserCartItem")
    @ResponseBody
    List<OrderItemsVo> getCurrentUserCartItem();
}
