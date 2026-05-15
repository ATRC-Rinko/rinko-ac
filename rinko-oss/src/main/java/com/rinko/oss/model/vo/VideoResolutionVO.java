package com.rinko.oss.model.vo;

import com.rinko.oss.model.entity.VideoResolutionEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "视频分辨率VO")
public record VideoResolutionVO(
        @Schema(description = "主键ID") Long id,
        @Schema(description = "文件ID（关联 file_metadata）") long fileId,
        @Schema(description = "文件版本号") int version,
        @Schema(description = "视频分辨率（如 1920x1080）") String resolution,
        @Schema(description = "转码状态（PENDING / PROCESSING / COMPLETED / FAILED）") String status,
        @Schema(description = "转码后文件大小（字节）") Long fileSize,
        @Schema(description = "转码后存储路径") String storagePath,
        @Schema(description = "错误信息") String errorMessage,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {

    public static VideoResolutionVO from(VideoResolutionEntity entity) {
        return new VideoResolutionVO(
                entity.getId(),
                entity.getFileId(),
                entity.getVersion(),
                entity.getResolution(),
                entity.getStatus(),
                entity.getFileSize(),
                entity.getStoragePath(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
