package com.rinko.scheduler.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.scheduler.entity.SchedulerExecution;
import com.rinko.scheduler.entity.SchedulerJob;
import com.rinko.scheduler.service.SchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scheduler")
@Tag(name = "Scheduler", description = "任务调度管理接口")
public class SchedulerController {

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GetMapping("/jobs")
    @Operation(summary = "列出所有任务")
    public ApiResponse<List<SchedulerJob>> listJobs() {
        return ApiResponse.success(schedulerService.listJobs());
    }

    @PostMapping("/jobs")
    @Operation(summary = "创建任务")
    public ApiResponse<SchedulerJob> createJob(@RequestBody SchedulerJob job, HttpServletResponse response) {
        response.setStatus(201);
        return ApiResponse.success(schedulerService.createJob(job));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "删除任务")
    public ApiResponse<Void> deleteJob(@PathVariable long id) {
        schedulerService.deleteJob(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/jobs/{id}/trigger")
    @Operation(summary = "立即触发任务")
    public ApiResponse<Void> triggerJob(@PathVariable long id) {
        schedulerService.triggerJob(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/jobs/{id}/pause")
    @Operation(summary = "暂停任务")
    public ApiResponse<Void> pauseJob(@PathVariable long id) {
        schedulerService.pauseJob(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/jobs/{id}/resume")
    @Operation(summary = "恢复任务")
    public ApiResponse<Void> resumeJob(@PathVariable long id) {
        schedulerService.resumeJob(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/executions")
    @Operation(summary = "查询执行历史")
    public ApiResponse<List<SchedulerExecution>> getExecutions(@RequestParam long jobId) {
        return ApiResponse.success(schedulerService.getExecutions(jobId));
    }
}
