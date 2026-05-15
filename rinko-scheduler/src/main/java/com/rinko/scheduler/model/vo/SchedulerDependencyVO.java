package com.rinko.scheduler.model.vo;

import com.rinko.scheduler.model.entity.SchedulerDependency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "调度依赖关系VO")
public record SchedulerDependencyVO(
        @Schema(description = "主键ID") Long id,
        @Schema(description = "当前任务ID") long jobId,
        @Schema(description = "依赖的前置任务ID") long dependsOnJobId,
        @Schema(description = "创建时间") LocalDateTime createdAt) {

    public static SchedulerDependencyVO from(SchedulerDependency entity) {
        return new SchedulerDependencyVO(
                entity.getId(),
                entity.getJobId(),
                entity.getDependsOnJobId(),
                entity.getCreatedAt()
        );
    }
}
