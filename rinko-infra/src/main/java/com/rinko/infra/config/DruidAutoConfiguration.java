package com.rinko.infra.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Druid 数据源自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(DruidDataSource.class)
@EnableConfigurationProperties(DruidDataSourceProperties.class)
public class DruidAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DruidAutoConfiguration.class);

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.druid", name = "enabled", matchIfMissing = true)
    public DataSource druidDataSource(DruidDataSourceProperties properties) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setInitialSize(properties.getInitialSize());
        dataSource.setMinIdle(properties.getMinIdle());
        dataSource.setMaxActive(properties.getMaxActive());
        dataSource.setMaxWait(properties.getMaxWait());
        dataSource.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(properties.getMinEvictableIdleTimeMillis());
        dataSource.setMaxEvictableIdleTimeMillis(properties.getMaxEvictableIdleTimeMillis());

        List<Filter> filters = new ArrayList<>();
        if (properties.isStatFilter()) {
            StatFilter statFilter = new StatFilter();
            statFilter.setSlowSqlMillis(properties.getSlowSqlMillis());
            statFilter.setLogSlowSql(properties.isLogSlowSql());
            statFilter.setMergeSql(properties.isMergeSql());
            filters.add(statFilter);
        }
        dataSource.setProxyFilters(filters);

        log.info("Druid DataSource configured: maxActive={}, minIdle={}", properties.getMaxActive(),
                properties.getMinIdle());
        return dataSource;
    }
}
