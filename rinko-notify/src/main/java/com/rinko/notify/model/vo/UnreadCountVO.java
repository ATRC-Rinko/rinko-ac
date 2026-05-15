package com.rinko.notify.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "未读消息数")
public record UnreadCountVO(
        @Schema(description = "未读数量") long count) {
}
