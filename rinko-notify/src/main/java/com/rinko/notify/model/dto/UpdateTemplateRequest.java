package com.rinko.notify.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "更新通知模板请求")
public record UpdateTemplateRequest(
        @Schema(description = "模板名称") String name,
        @Schema(description = "模板主题") String subject,
        @Schema(description = "模板正文") String body,
        @Schema(description = "适用渠道") String channels) {
}
