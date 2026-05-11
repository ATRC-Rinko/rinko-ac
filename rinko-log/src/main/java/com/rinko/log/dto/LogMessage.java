package com.rinko.log.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Kafka 接收的日志消息 DTO，映射 JSON 日志格式。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LogMessage(
        String timestamp,
        String level,
        String service,
        String traceId,
        String spanId,
        @JsonProperty("class") String className,
        String message,
        String thread,
        Map<String, Object> context,
        String exception,
        String exceptionClass
) {
}
