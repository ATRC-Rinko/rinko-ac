package com.rinko.log.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "设置日志级别请求")
public record SetLogLevelRequest(
        @Schema(description = "目标服务名", example = "rinko-auth") String service,
        @Schema(description = "日志记录器名称", example = "com.rinko.auth") String logger,
        @Schema(description = "日志级别", example = "DEBUG") String level) {
}
