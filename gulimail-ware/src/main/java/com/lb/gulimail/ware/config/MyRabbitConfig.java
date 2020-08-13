package com.lb.gulimail.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitConfig {
    private static final String DELAY_QUEUE_NAME="stock.delay.queue";
    private static final String RELEASE_QUEUE_NAME="stock.release.stock.queue";
    private static final String STOCK_EXCHANGE_NAME="stock-event-exchange";
    private static final String RELEASE_KEY="stock.release";
    private static final String DELAY_KEY="stock.locked";
    private static final Long TTL=120000l;

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 配置rabbitmq使用json进行消息转换
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange(){
       return new TopicExchange(STOCK_EXCHANGE_NAME,true,false);
    }
    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue(RELEASE_QUEUE_NAME,true,false,false);
    }
    @Bean
    public Queue stockDelayQueue(){
        Map<String,Object> args=new HashMap<>();
        args.put("x-dead-letter-exchange",STOCK_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key",RELEASE_KEY);
        args.put("x-message-ttl",TTL);
        return new Queue(DELAY_QUEUE_NAME,true,false,false,args);
    }
    @Bean
    public Binding stockReleaseBinding(){
        return new Binding(RELEASE_QUEUE_NAME, Binding.DestinationType.QUEUE,STOCK_EXCHANGE_NAME,RELEASE_KEY,null);
    }

    @Bean
    public Binding stockDelayBinding(){
        return new Binding(DELAY_QUEUE_NAME, Binding.DestinationType.QUEUE,STOCK_EXCHANGE_NAME,DELAY_KEY,null);
    }

}
