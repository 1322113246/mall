package com.lb.gulimail.order.listener;

import com.lb.gulimail.order.entity.OrderEntity;
import com.lb.gulimail.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RabbitListener(queues = "order.release.queue")
@Service
public class OrderCloseListener {
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void  listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        try {
            System.out.println("收到过期的订单信息:准备关闭订单"+orderEntity.getOrderSn());
            orderService.closeOrder(orderEntity);
            //未防止关单同时 支付成功异步消息还没到 顶单却关了，所以我们可以在订单关闭同时，手动调用支付宝支付失败

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            System.out.println("关闭订单失败,错误信息："+e.getMessage());
            try {
                Thread.sleep(10000);
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            } catch (InterruptedException interruptedException) {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
                interruptedException.printStackTrace();
            }
        }

    }

}
