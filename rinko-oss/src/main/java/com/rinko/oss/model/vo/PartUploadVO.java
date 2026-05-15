package com.rinko.oss.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分片上传响应")
public record PartUploadVO(
        @Schema(description = "分片编号") String partNumber,
        @Schema(description = "分片ETag") String etag) {
}
