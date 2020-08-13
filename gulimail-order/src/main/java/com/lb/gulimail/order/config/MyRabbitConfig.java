package com.lb.gulimail.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 配置rabbitmq使用json进行消息转换
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
    /**
     * 定制rabbitTemplate
     */
    @PostConstruct//MyRabbitConfig对象创建完成后，执行这个方法
    public void initRabbitTemplate(){
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 当前消息的唯一关联数据 (这个是消息的唯一id)
             * @param b 消息是否成功到达
             * @param s 失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                System.out.println("confirm..correlationData["+correlationData+"]==>ack["+b+"]==>cause["+s+"]");
            }
        });
        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 消息没有投递给指定队列，就会触发这个失败回调
             * @param message 失败信息
             * @param replyCode 状态码
             * @param replyText 回复的文本内容
             * @param exchange  发送到哪个交换机
             * @param routingKey  用的哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("fail msg:"+message+"==>replyCode:"+replyCode+"==>replyText:"+replyText+"==>exchange:"+exchange+"==>routingKey:"+routingKey);
            }
        });
    }

}
