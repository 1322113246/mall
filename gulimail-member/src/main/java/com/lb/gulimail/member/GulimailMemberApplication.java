package com.lb.gulimail.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 远程调用别的服务
 * 1.引入open-feign依赖
 * 2.编写一个接口，告诉springcloud这个接口需要调用远程服务
 * 3.开启远程调用功能
 */
@EnableFeignClients(basePackages = "com.atguigu.gulimail.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.atguigu.gulimail.member.dao")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession
public class GulimailMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailMemberApplication.class, args);
    }

}
