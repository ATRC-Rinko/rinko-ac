package com.rinko.notify.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "批量发送通知响应")
public record SendBatchVo(
        @Schema(description = "发送数量") int count,
        @Schema(description = "通知ID列表") List<Long> notificationIds) {
}
