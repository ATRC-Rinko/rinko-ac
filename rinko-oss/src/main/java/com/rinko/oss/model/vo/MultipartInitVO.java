package com.rinko.oss.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分片上传初始化响应")
public record MultipartInitVO(
        @Schema(description = "上传会话ID") String uploadId,
        @Schema(description = "文件ID") String fileId) {
}
