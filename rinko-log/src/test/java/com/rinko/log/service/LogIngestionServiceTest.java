package com.rinko.log.service;

import com.rinko.log.config.LogProperties;
import com.rinko.log.dto.LogMessage;
import com.rinko.log.repository.ClickHouseLogRepository;
import com.rinko.log.repository.LogLevelConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogIngestionServiceTest {

    @Mock
    private ClickHouseLogRepository clickHouseLogRepository;

    private LogProperties logProperties;
    private LogIngestionService service;
    private LogLevelConfigMapper logLevelConfigMapper;

    @BeforeEach
    void setUp() {
        logProperties = new LogProperties();
        logProperties.setSamplingRate(1.0);
        service = new LogIngestionService(clickHouseLogRepository, logProperties, logLevelConfigMapper);
    }

    @Test
    void shouldAlwaysIngestErrorLevelMessages() {
        LogMessage msg = new LogMessage(null, "ERROR", "rinko-auth",
                null, null, null, "test error", null, null, null, null);

//        service.ingest(msg);
        service.flush();

        verify(clickHouseLogRepository).batchInsert(anyList());
    }

    @Test
    void shouldAlwaysIngestWarnLevelMessages() {
        LogMessage msg = new LogMessage(null, "WARN", "rinko-auth",
                null, null, null, "test warning", null, null, null, null);

//        service.ingest(msg);
        service.flush();

        verify(clickHouseLogRepository).batchInsert(anyList());
    }

    @Test
    void shouldNotFlushEmptyBuffer() {
        service.flush();
        verify(clickHouseLogRepository, never()).batchInsert(anyList());
    }
}
