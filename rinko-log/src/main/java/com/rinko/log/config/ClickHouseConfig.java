package com.rinko.log.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * ClickHouse JDBC 数据源配置。
 * 不暴露 DataSource Bean，避免 MyBatis-Plus 误用。
 */
@Configuration
@EnableConfigurationProperties(LogProperties.class)
public class ClickHouseConfig {

    @Bean
    public JdbcTemplate clickHouseJdbcTemplate(LogProperties logProperties) {
        LogProperties.ClickHouse ch = logProperties.getClickhouse();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(ch.getJdbcUrl());
        dataSource.setUsername(ch.getUsername());
        dataSource.setPassword(ch.getPassword());
        dataSource.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        return new JdbcTemplate(dataSource);
    }
}
