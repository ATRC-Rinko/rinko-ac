package com.rinko.log.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 动态日志级别配置实体，映射 PostgreSQL log_level_configs 表。
 */
@Data
@Table("log_level_configs")
public class LogLevelConfig {

    @Id
    private Long id;
    private String serviceName;
    private String loggerName;
    private String logLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
