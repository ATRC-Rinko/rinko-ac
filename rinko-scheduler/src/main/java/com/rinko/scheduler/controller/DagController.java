package com.rinko.scheduler.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.scheduler.model.dto.AddDependencyRequest;
import com.rinko.scheduler.model.vo.SchedulerDependencyVO;
import com.rinko.scheduler.service.SchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ApiResponse<SchedulerDependencyVO> addDependency(
            @PathVariable long jobId, @RequestBody AddDependencyRequest req, HttpServletResponse response) {
        response.setStatus(201);
        return ApiResponse.success(SchedulerDependencyVO.from(schedulerService.addDependency(jobId, req.dependsOnJobId())));
    }

    @DeleteMapping("/dependencies/{depId}")
    @Operation(summary = "删除依赖")
    public ApiResponse<Void> removeDependency(@PathVariable long depId) {
        schedulerService.removeDependency(depId);
        return ApiResponse.success(null);
    }

    @GetMapping("/jobs/{jobId}/downstream")
    @Operation(summary = "查询下游任务")
    public ApiResponse<List<SchedulerDependencyVO>> getDownstream(
            @PathVariable long jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(schedulerService.getDownstream(jobId).stream()
                .map(SchedulerDependencyVO::from)
                .toList());
    }

    @GetMapping("/jobs/{jobId}/upstream")
    @Operation(summary = "查询上游任务")
    public ApiResponse<List<SchedulerDependencyVO>> getUpstream(
            @PathVariable long jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(schedulerService.getUpstream(jobId).stream()
                .map(SchedulerDependencyVO::from)
                .toList());
    }
}
