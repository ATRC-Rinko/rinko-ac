package com.rinko.log.model.entity;

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
    /** 主键ID */
    private Long id;
    /** 服务名称 */
    private String serviceName;
    /** 日志记录器名称 */
    private String loggerName;
    /** 日志级别（TRACE | DEBUG | INFO | WARN | ERROR） */
    private String logLevel;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
