package com.rinko.log.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志条目实体，映射 ClickHouse logs 表查询结果。
 */
@Data
public class LogEntry {

    /** 日志时间戳 */
    private LocalDateTime timestamp;
    /** 日志级别 */
    private String level;
    /** 服务名称 */
    private String service;
    /** 链路追踪ID */
    private String traceId;
    /** 链路跨度ID */
    private String spanId;
    /** 类名 */
    private String className;
    /** 日志消息内容 */
    private String message;
    /** 线程名称 */
    private String thread;
    /** 上下文信息（JSON格式） */
    private String context;
    /** 异常信息 */
    private String exception;
    /** 异常类名 */
    private String exceptionClass;
}
