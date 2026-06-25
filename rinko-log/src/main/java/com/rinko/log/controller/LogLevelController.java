package com.rinko.log.controller;

import com.rinko.log.model.dto.SetLogLevelRequest;
import com.rinko.log.model.vo.LogLevelConfigVO;
import com.rinko.log.service.LogLevelManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs/levels")
@Tag(name = "Log Level Management", description = "动态日志级别管理接口")
public class LogLevelController {

    private final LogLevelManagementService logLevelManagementService;

    public LogLevelController(LogLevelManagementService logLevelManagementService) {
        this.logLevelManagementService = logLevelManagementService;
    }

    @GetMapping
    @Operation(summary = "获取所有日志级别配置")
    public List<LogLevelConfigVO> getLogLevels() {
        return logLevelManagementService.getAllConfigs().stream()
                .map(LogLevelConfigVO::from)
                .toList();
    }

    @PutMapping
    @Operation(summary = "修改日志级别")
    @ResponseStatus(HttpStatus.OK)
    public LogLevelConfigVO setLogLevel(@RequestBody SetLogLevelRequest req) {
        return LogLevelConfigVO.from(logLevelManagementService.setLogLevel(
                req.service(), req.loggerName(), req.level()));
    }

    @DeleteMapping
    @Operation(summary = "重置日志级别")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetLogLevel(@RequestParam String service, @RequestParam String loggerName) {
        logLevelManagementService.resetLogLevel(service, loggerName);
    }
}
