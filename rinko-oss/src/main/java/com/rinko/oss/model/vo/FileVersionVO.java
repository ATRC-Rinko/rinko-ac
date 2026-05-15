package com.rinko.oss.model.vo;

import com.rinko.oss.model.entity.FileVersion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "文件版本VO")
public record FileVersionVO(
        @Schema(description = "主键ID") Long id,
        @Schema(description = "文件ID（关联 file_metadata）") long fileId,
        @Schema(description = "版本号") int version,
        @Schema(description = "文件大小（字节）") long fileSize,
        @Schema(description = "文件SHA256哈希值") String sha256,
        @Schema(description = "存储路径") String storagePath,
        @Schema(description = "创建时间") LocalDateTime createdAt) {

    public static FileVersionVO from(FileVersion entity) {
        return new FileVersionVO(
                entity.getId(),
                entity.getFileId(),
                entity.getVersion(),
                entity.getFileSize(),
                entity.getSha256(),
                entity.getStoragePath(),
                entity.getCreatedAt()
        );
    }
}
