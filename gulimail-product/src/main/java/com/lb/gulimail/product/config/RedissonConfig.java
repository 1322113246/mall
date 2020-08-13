package com.lb.gulimail.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(){
        // 默认连接地址 127.0.0.1:6379
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.52.128:6379");

        return Redisson.create(config);
    }
}