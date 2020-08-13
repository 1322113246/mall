package com.lb.gulimail.member.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lb.common.utils.Constant;
import com.lb.common.utils.PageUtils;
import com.lb.gulimail.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

@Controller
public class MemberWebController {
    @Autowired
    OrderFeignService orderFeignService;
    @GetMapping("memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum, Model model){
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constant.PAGE,pageNum.toString());
        PageUtils pageUtils = orderFeignService.listWithItem(map).getData(new TypeReference<PageUtils>() {
        });
        model.addAttribute("orders",pageUtils);
        String jsonString = JSON.toJSONString(pageUtils);
        System.out.println(jsonString);
        return "orderList";
    }

}
