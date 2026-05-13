package com.rinko.log.controller;

import com.rinko.infra.dto.PageResponse;
import com.rinko.log.entity.LogEntry;
import com.rinko.log.service.LogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 日志查询 API。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs")
@Tag(name = "Log Query", description = "日志查询接口")
public class LogQueryController {

    private final LogQueryService logQueryService;


    @GetMapping
    @Operation(summary = "查询日志")
    public PageResponse<LogEntry> queryLogs(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return logQueryService.queryLogs(startTime, endTime, level, service, traceId, keyword, page, size);
    }
}
