package com.rinko.log.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志服务配置属性。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "rinko.log")
public class LogProperties {

    /** 日志采样率 0.0-1.0，默认 1.0（全量），WARN/ERROR 始终保留 */
    private double samplingRate = 1.0;

    /** ClickHouse 连接配置 */
    private ClickHouse clickhouse = new ClickHouse();

    @Getter
    @Setter
    public static class ClickHouse {
        private String host = "localhost";
        private int port = 8123;
        private String database = "rinko_log";
        private String username = "rinko";
        private String password = "anchorage";

        public String getJdbcUrl() {
            return "jdbc:clickhouse:http://" + host + ":" + port + "/" + database;
        }
    }
}
