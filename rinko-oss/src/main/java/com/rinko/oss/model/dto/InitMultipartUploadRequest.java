package com.rinko.oss.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "初始化分片上传请求")
public record InitMultipartUploadRequest(
        @Schema(description = "原始文件名", example = "video.mp4") String originalName,
        @Schema(description = "文件MIME类型", example = "video/mp4", defaultValue = "application/octet-stream") String contentType,
        @Schema(description = "父目录ID（可选）") Long parentId) {
}
