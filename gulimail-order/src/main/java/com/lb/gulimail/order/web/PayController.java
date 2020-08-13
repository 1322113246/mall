package com.lb.gulimail.order.web;

import com.alipay.api.AlipayApiException;
import com.lb.gulimail.order.config.AlipayTemplate;
import com.lb.gulimail.order.service.OrderService;
import com.lb.gulimail.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayController {
    @Autowired
    AlipayTemplate alipayTemplate;
    @Autowired
    OrderService orderService;
    @GetMapping(value = "payOrder",produces = "text/html")
    @ResponseBody
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo=orderService.getOrderPay(orderSn);
//        payVo.setBody();//订单备注
//        payVo.setOut_trade_no()//订单号;
//        payVo.setSubject();//订单主题
//        payVo.setTotal_amount();
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);
        return pay;
    }

}
