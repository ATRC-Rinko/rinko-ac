package com.rinko.notify.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "创建通知模板请求")
public record CreateTemplateRequest(
        @NotBlank(message = "Template code is required")
        @Schema(description = "模板编码（唯一）", example = "welcome") String code,
        @NotBlank(message = "Template name is required")
        @Schema(description = "模板名称", example = "欢迎通知") String name,
        @Schema(description = "模板主题", example = "欢迎 {username}") String subject,
        @Schema(description = "模板正文", example = "你好 {username}，欢迎使用 Rinko！") String body,
        @Schema(description = "适用渠道", example = "IN_APP", defaultValue = "IN_APP") String channels) {
}
