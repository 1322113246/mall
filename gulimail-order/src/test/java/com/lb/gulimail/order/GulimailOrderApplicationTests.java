package com.lb.gulimail.order;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GulimailOrderApplicationTests {
    @Autowired
    RabbitTemplate rabbitTemplate;

}
