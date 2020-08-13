package com.lb.gulimail.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MQConfig {


    @Bean
    public Queue orderDelayQueue(){
        Map<String,Object> args=new HashMap<>();
        args.put("x-dead-letter-exchange","order-event-exchange");
        args.put("x-dead-letter-routing-key","order.release.order");
        args.put("x-message-ttl",60000);
        Queue queue = new Queue("order.delay.queue",true,false,false,args);
        return queue;
    }
    @Bean
    public Queue orderReleaseQueue(){
        return new Queue("order.release.queue",true,false,false);
    }
    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }
    @Bean
    public Binding orderCreateOrderBinding(){
        Binding binding = new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order",null);
        return binding;
    }
    @Bean
    public Binding orderReleaseOrderBinding(){
        Binding binding = new Binding("order.release.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order",null);
        return binding;
    }

    /**
     * 订单释放和库存释放绑定
     */
    @Bean
    public Binding orderReleaseOther(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE
        ,"order-event-exchange","order.release.other.#",null);
    }
}
