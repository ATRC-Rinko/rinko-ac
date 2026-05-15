package com.rinko.scheduler.model.vo;

import com.rinko.scheduler.model.entity.SchedulerExecution;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "调度执行记录VO")
public record SchedulerExecutionVO(
        @Schema(description = "主键ID") Long id,
        @Schema(description = "任务ID（关联 scheduler_jobs）") long jobId,
        @Schema(description = "执行状态（RUNNING / SUCCESS / FAILED）") String status,
        @Schema(description = "开始时间") LocalDateTime startTime,
        @Schema(description = "结束时间") LocalDateTime endTime,
        @Schema(description = "重试次数") int retryCount,
        @Schema(description = "执行结果") String result,
        @Schema(description = "创建时间") LocalDateTime createdAt) {

    public static SchedulerExecutionVO from(SchedulerExecution entity) {
        return new SchedulerExecutionVO(
                entity.getId(),
                entity.getJobId(),
                entity.getStatus(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getRetryCount(),
                entity.getResult(),
                entity.getCreatedAt()
        );
    }
}
