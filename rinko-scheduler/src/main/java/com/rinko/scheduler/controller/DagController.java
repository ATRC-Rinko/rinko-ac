package com.rinko.scheduler.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.scheduler.entity.SchedulerDependency;
import com.rinko.scheduler.service.SchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scheduler")
@Tag(name = "DAG", description = "任务依赖管理接口")
public class DagController {

    private final SchedulerService schedulerService;

    public DagController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/jobs/{jobId}/dependencies")
    @Operation(summary = "添加任务依赖")
    public ApiResponse<SchedulerDependency> addDependency(
            @PathVariable long jobId, @RequestBody Map<String, Long> body, HttpServletResponse response) {
        Long dependsOn = body.get("dependsOnJobId");
        response.setStatus(201);
        return ApiResponse.success(schedulerService.addDependency(jobId, dependsOn));
    }

    @DeleteMapping("/dependencies/{depId}")
    @Operation(summary = "删除依赖")
    public ApiResponse<Void> removeDependency(@PathVariable long depId) {
        schedulerService.removeDependency(depId);
        return ApiResponse.success(null);
    }

    @GetMapping("/jobs/{jobId}/downstream")
    @Operation(summary = "查询下游任务")
    public ApiResponse<List<SchedulerDependency>> getDownstream(@PathVariable long jobId) {
        return ApiResponse.success(schedulerService.getDownstream(jobId));
    }

    @GetMapping("/jobs/{jobId}/upstream")
    @Operation(summary = "查询上游任务")
    public ApiResponse<List<SchedulerDependency>> getUpstream(@PathVariable long jobId) {
        return ApiResponse.success(schedulerService.getUpstream(jobId));
    }
}
