package com.rinko.log.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * ClickHouse JDBC 数据源配置（HikariCP 连接池）。
 * 不暴露 DataSource Bean，避免 MyBatis-Plus 误用。
 */
@Configuration
@EnableConfigurationProperties(LogProperties.class)
public class ClickHouseConfig {

    @Bean
    public JdbcTemplate clickHouseJdbcTemplate(LogProperties logProperties) {
        LogProperties.ClickHouse ch = logProperties.getClickhouse();
        DataSource dataSource = clickHouseDataSource(ch);
        return new JdbcTemplate(dataSource);
    }

    private DataSource clickHouseDataSource(LogProperties.ClickHouse ch) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(ch.getJdbcUrl());
        config.setUsername(ch.getUsername());
        config.setPassword(ch.getPassword());
        config.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        config.setPoolName("ClickHousePool");
        return new HikariDataSource(config);
    }
}
