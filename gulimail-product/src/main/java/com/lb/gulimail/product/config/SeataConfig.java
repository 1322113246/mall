package com.lb.gulimail.product.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
public class SeataConfig {
    @Autowired
    DataSourceProperties properties;
    @Primary
    @Bean
    public DataSource dataSource(DataSourceProperties properties){
        HikariDataSource dataSource = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if (StringUtils.hasText(properties.getName())){
            dataSource.setPoolName(properties.getName());
        }
        return new DataSourceProxy(dataSource);
    }
}
