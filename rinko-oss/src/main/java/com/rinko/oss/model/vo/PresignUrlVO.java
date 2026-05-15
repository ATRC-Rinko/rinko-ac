package com.rinko.oss.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "预签名URL响应")
public record PresignUrlVO(
        @Schema(description = "预签名下载URL") String url) {
}
