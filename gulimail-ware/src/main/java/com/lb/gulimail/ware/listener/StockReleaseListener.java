package com.lb.gulimail.ware.listener;

import com.lb.common.to.OrderEntityTo;
import com.lb.common.to.mq.StockLockedTo;
import com.lb.gulimail.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;


    /**
     * 处理库存释放
     *
     */
    @RabbitHandler
    public void  handleStockRelease(StockLockedTo stockLockedTo, Channel channel, Message message) throws IOException {
        try {
            System.out.println("解锁消息收到------------");
            wareSkuService.unLockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            System.out.println(e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
    @RabbitHandler
    public void handleOrderClose(OrderEntityTo orderEntityTo, Message message, Channel channel) throws IOException {
        System.out.println("订单关闭，解锁库存");
        try {
            wareSkuService.unLockStock(orderEntityTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
