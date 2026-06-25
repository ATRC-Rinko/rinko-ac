package com.rinko.notify.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "发送通知请求")
public record SendNotificationRequest(
        @Schema(description = "通知渠道", example = "EMAIL") String channel,
        @Schema(description = "模板编码", example = "welcome") String templateCode,
        @Schema(description = "收件人", example = "user@example.com") String recipient,
        @Schema(description = "模板变量替换") Map<String, String> variables) {
}
