package com.rinko.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Druid 数据源配置属性。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.datasource.druid")
public class DruidDataSourceProperties {

    /** 初始连接数 */
    private int initialSize = 5;
    /** 最小空闲连接数 */
    private int minIdle = 5;
    /** 最大活跃连接数 */
    private int maxActive = 20;
    /** 获取连接超时时间（毫秒） */
    private long maxWait = 60000;
    /** 连接空闲检测间隔（毫秒） */
    private long timeBetweenEvictionRunsMillis = 60000;
    /** 连接最小空闲时间（毫秒） */
    private long minEvictableIdleTimeMillis = 300000;
    /** 连接最大存活时间（毫秒） */
    private long maxEvictableIdleTimeMillis = 900000;
    /** 监控过滤器是否启用 */
    private boolean statFilter = true;
    /** 慢 SQL 阈值（毫秒） */
    private long slowSqlMillis = 1000;
    /** 是否记录慢 SQL */
    private boolean logSlowSql = true;
    /** 是否合并 SQL */
    private boolean mergeSql = true;
}
