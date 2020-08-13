package com.lb.gulimail.order.web;

import com.lb.gulimail.order.service.OrderService;
import com.lb.gulimail.order.vo.OrderConfirmVo;
import com.lb.gulimail.order.vo.OrderSubmitVo;
import com.lb.gulimail.order.vo.SubmitOrderRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    /**
     * 订单确认页
     * @param model
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo=orderService.confirmOrder();
        model.addAttribute("data", confirmVo);
        return "confirm";
    }
    /**
     * 订单提交
     */

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model){
        System.out.println("订单提交的数据:"+vo);
        SubmitOrderRespVo submitOrderRespVo=orderService.submitOrder(vo);
        if (submitOrderRespVo.getCode()==0){
            //订单生成成功
            model.addAttribute("data",submitOrderRespVo);
            System.out.println("订单成功数据:"+submitOrderRespVo);
            return "pay";
        }else{
            String msg="";
            switch (submitOrderRespVo.getCode()){
                case 1:msg="订单信息过期，请刷新再次提交";break;
                case 2:msg="订单商品价格发生变化";break;
                case 3:msg="库存不足";break;
            }
            model.addAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

}
