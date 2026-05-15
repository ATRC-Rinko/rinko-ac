package com.rinko.oss.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "完成分片上传请求")
public record CompleteMultipartUploadRequest(
        @Schema(description = "上传会话ID") String uploadId,
        @Schema(description = "分片列表") List<PartInfo> parts) {

    @Schema(description = "分片信息")
    public record PartInfo(
            @Schema(description = "分片编号") int partNumber,
            @Schema(description = "分片ETag") String etag) {
    }
}
