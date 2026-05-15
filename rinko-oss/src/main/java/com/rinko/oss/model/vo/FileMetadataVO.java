package com.rinko.oss.model.vo;

import com.rinko.oss.model.entity.FileMetadata;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "文件元数据VO")
public record FileMetadataVO(
        @Schema(description = "主键ID") Long id,
        @Schema(description = "原始文件名") String originalName,
        @Schema(description = "文件大小（字节）") long fileSize,
        @Schema(description = "文件内容类型（MIME）") String contentType,
        @Schema(description = "存储路径") String storagePath,
        @Schema(description = "存储类型（LOCAL / S3 / OSS）") String storageType,
        @Schema(description = "父目录ID") Long parentId,
        @Schema(description = "是否为目录") boolean isDirectory,
        @Schema(description = "当前版本号") int currentVersion,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {

    public static FileMetadataVO from(FileMetadata entity) {
        return new FileMetadataVO(
                entity.getId(),
                entity.getOriginalName(),
                entity.getFileSize(),
                entity.getContentType(),
                entity.getStoragePath(),
                entity.getStorageType(),
                entity.getParentId(),
                entity.isDirectory(),
                entity.getCurrentVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
