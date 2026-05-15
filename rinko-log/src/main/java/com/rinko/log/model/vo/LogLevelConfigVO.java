package com.rinko.log.model.vo;

import com.rinko.log.model.entity.LogLevelConfig;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "日志级别配置VO")
public record LogLevelConfigVO(
        @Schema(description = "主键ID") Long id,
        @Schema(description = "服务名称") String serviceName,
        @Schema(description = "日志记录器名称") String loggerName,
        @Schema(description = "日志级别（TRACE | DEBUG | INFO | WARN | ERROR）") String logLevel,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {

    public static LogLevelConfigVO from(LogLevelConfig entity) {
        return new LogLevelConfigVO(
                entity.getId(),
                entity.getServiceName(),
                entity.getLoggerName(),
                entity.getLogLevel(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
