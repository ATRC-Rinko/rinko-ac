package com.rinko.log.controller;

import com.rinko.log.model.dto.SetLogLevelRequest;
import com.rinko.log.model.vo.LogLevelConfigVO;
import com.rinko.log.service.LogLevelManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<LogLevelConfigVO> getLogLevels() {
        return logLevelManagementService.getAllConfigs().stream()
                .map(LogLevelConfigVO::from)
                .toList();
    }

    @PutMapping("/levels")
    @Operation(summary = "修改日志级别")
    public LogLevelConfigVO setLogLevel(@RequestBody SetLogLevelRequest req) {
        return LogLevelConfigVO.from(logLevelManagementService.setLogLevel(req.service(), req.logger(), req.level()));
    }

    @DeleteMapping("/levels/{service}/{logger}")
    @Operation(summary = "重置日志级别")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetLogLevel(@PathVariable String service, @PathVariable String logger) {
        logLevelManagementService.resetLogLevel(service, logger);
    }
}
