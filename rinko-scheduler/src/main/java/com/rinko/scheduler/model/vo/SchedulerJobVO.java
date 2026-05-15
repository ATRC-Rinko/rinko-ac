package com.rinko.scheduler.model.vo;

import com.rinko.scheduler.model.entity.SchedulerJob;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "调度任务VO")
public record SchedulerJobVO(
        @Schema(description = "主键ID") Long id,
        @Schema(description = "任务名称（唯一）") String name,
        @Schema(description = "任务类型") String type,
        @Schema(description = "CRON表达式") String cronExpression,
        @Schema(description = "任务配置（JSON格式）") String config,
        @Schema(description = "是否启用") boolean enabled,
        @Schema(description = "最大重试次数") int maxRetries,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {

    public static SchedulerJobVO from(SchedulerJob entity) {
        return new SchedulerJobVO(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getCronExpression(),
                entity.getConfig(),
                entity.isEnabled(),
                entity.getMaxRetries(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
