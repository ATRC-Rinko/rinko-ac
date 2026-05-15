package com.rinko.log.service;

import com.rinko.infra.dto.PageResponse;
import com.rinko.log.model.entity.LogEntry;
import com.rinko.log.repository.ClickHouseLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogQueryServiceTest {

    @Mock
    private ClickHouseLogRepository clickHouseLogRepository;

    @Test
    void shouldReturnPageResponseWithCorrectStructure() {
        LogQueryService service = new LogQueryService(clickHouseLogRepository);

        when(clickHouseLogRepository.countLogs(anyString(), anyString(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(100L);
        when(clickHouseLogRepository.queryLogs(anyString(), anyString(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(Collections.emptyList());

        PageResponse<LogEntry> result = service.queryLogs(
                "2026-05-01T00:00:00", "2026-05-02T00:00:00",
                null, null, null, null, 1, 20);

        assertNotNull(result);
        assertEquals(100L, result.totalElements());
        assertEquals(5, result.totalPages()); // 100/20 = 5
        assertEquals(1, result.page());
    }
}
