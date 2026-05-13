package com.rinko.log.controller;

import com.rinko.log.entity.LogLevelConfig;
import com.rinko.log.service.LogLevelManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 动态日志级别管理 API。
 */
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Log Level Management", description = "动态日志级别管理接口")
public class LogLevelController {

    private final LogLevelManagementService logLevelManagementService;

    @GetMapping("/levels")
    @Operation(summary = "获取所有日志级别配置")
    public Iterable<LogLevelConfig> getLogLevels() {
        return logLevelManagementService.getAllConfigs();
    }

    @PutMapping("/levels")
    @Operation(summary = "修改日志级别")
    public LogLevelConfig setLogLevel(@RequestBody Map<String, String> body) {
        String service = body.get("service");
        String logger = body.get("logger");
        String level = body.get("level");
        return logLevelManagementService.setLogLevel(service, logger, level);
    }

    @DeleteMapping("/levels/{service}/{logger}")
    @Operation(summary = "重置日志级别")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetLogLevel(@PathVariable String service, @PathVariable String logger) {
        logLevelManagementService.resetLogLevel(service, logger);
    }
}
