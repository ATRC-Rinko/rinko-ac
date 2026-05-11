package com.rinko.log.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志条目实体，映射 ClickHouse logs 表查询结果。
 */
@Data
public class LogEntry {

    private LocalDateTime timestamp;
    private String level;
    private String service;
    private String traceId;
    private String spanId;
    private String className;
    private String message;
    private String thread;
    private String context;
    private String exception;
    private String exceptionClass;
}
