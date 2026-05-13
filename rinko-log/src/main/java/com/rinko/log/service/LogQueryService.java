package com.rinko.log.service;

import com.rinko.infra.dto.PageResponse;
import com.rinko.log.entity.LogEntry;
import com.rinko.log.repository.ClickHouseLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 日志查询服务。
 */
@Service
@RequiredArgsConstructor
public class LogQueryService {

    private static final long MAX_RANGE_DAYS = 7;

    private final ClickHouseLogRepository clickHouseLogRepository;

    /**
     * 分页查询日志，校验时间范围不超过 7 天。
     */
    public PageResponse<LogEntry> queryLogs(String startTime, String endTime, String level,
                                              String service, String traceId, String keyword,
                                              int page, int size) {
        long totalElements = clickHouseLogRepository.countLogs(startTime, endTime, level, service, traceId, keyword);
        int offset = (page - 1) * size;
        List<LogEntry> content = clickHouseLogRepository.queryLogs(startTime, endTime, level, service, traceId, keyword, offset, size);
        return new PageResponse<>(content, totalElements, page, size);
    }
}
