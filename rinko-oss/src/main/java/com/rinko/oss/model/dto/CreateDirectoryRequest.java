package com.rinko.oss.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "创建目录请求")
public record CreateDirectoryRequest(
        @Schema(description = "目录名称", example = "docs") String name,
        @Schema(description = "父目录ID（可选，不传则创建在根目录）") Long parentId) {
}
