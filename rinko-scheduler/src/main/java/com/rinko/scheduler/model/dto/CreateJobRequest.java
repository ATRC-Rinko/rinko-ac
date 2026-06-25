package com.rinko.scheduler.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建调度任务的请求 DTO。替代直接绑定 SchedulerJob 实体。
 */
public record CreateJobRequest(
        @NotBlank(message = "Job name is required")
        String name,

        @NotBlank(message = "Job type is required")
        String type,

        String cronExpression,

        String config,

        boolean enabled,

        int maxRetries
) {
}
