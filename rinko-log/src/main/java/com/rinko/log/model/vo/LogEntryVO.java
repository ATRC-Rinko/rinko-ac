package com.rinko.log.model.vo;

import com.rinko.log.model.entity.LogEntry;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "日志条目VO")
public record LogEntryVO(
        @Schema(description = "日志时间戳") LocalDateTime timestamp,
        @Schema(description = "日志级别") String level,
        @Schema(description = "服务名称") String service,
        @Schema(description = "链路追踪ID") String traceId,
        @Schema(description = "链路跨度ID") String spanId,
        @Schema(description = "类名") String className,
        @Schema(description = "日志消息内容") String message,
        @Schema(description = "线程名称") String thread,
        @Schema(description = "上下文信息（JSON格式）") String context,
        @Schema(description = "异常信息") String exception,
        @Schema(description = "异常类名") String exceptionClass) {

    public static LogEntryVO from(LogEntry entity) {
        return new LogEntryVO(
                entity.getTimestamp(),
                entity.getLevel(),
                entity.getService(),
                entity.getTraceId(),
                entity.getSpanId(),
                entity.getClassName(),
                entity.getMessage(),
                entity.getThread(),
                entity.getContext(),
                entity.getException(),
                entity.getExceptionClass()
        );
    }
}
