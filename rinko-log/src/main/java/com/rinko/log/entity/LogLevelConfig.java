package com.rinko.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态日志级别配置实体，映射 PostgreSQL log_level_configs 表。
 */
@Data
@TableName("log_level_configs")
public class LogLevelConfig {

    @TableId(type = IdType.INPUT)
    private Long id;
    private String serviceName;
    private String loggerName;
    private String logLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
