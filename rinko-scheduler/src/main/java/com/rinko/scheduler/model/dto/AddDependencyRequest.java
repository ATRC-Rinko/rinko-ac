package com.rinko.scheduler.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "添加任务依赖请求")
public record AddDependencyRequest(
        @Schema(description = "依赖的前置任务ID") long dependsOnJobId) {
}
