package com.rinko.log.service;

import com.rinko.log.config.LogProperties;
import com.rinko.log.dto.LogMessage;
import com.rinko.log.repository.ClickHouseLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 日志摄入服务：批量缓冲 + 定时冲刷 + 采样。
 */
@Service
@RequiredArgsConstructor
public class LogIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LogIngestionService.class);

    private final ClickHouseLogRepository clickHouseLogRepository;
    private final LogProperties logProperties;
    private final List<LogMessage> buffer = Collections.synchronizedList(new ArrayList<>());


    /**
     * 接收一条日志消息。根据采样率决定是否写入，WARN/ERROR 始终写入。
     */
    public void ingest(LogMessage message) {
        if (!shouldSample(message)) {
            return;
        }
        buffer.add(message);
        if (buffer.size() >= 100) {
            flush();
        }
    }

    /**
     * 批量接收日志消息。
     */
    public void ingestBatch(List<LogMessage> messages) {
        for (LogMessage msg : messages) {
            if (shouldSample(msg)) {
                buffer.add(msg);
            }
        }
        if (buffer.size() >= 1000) {
            flush();
        }
    }

    /**
     * 定时冲刷（每 5 秒）。
     */
    @Scheduled(fixedRate = 5000)
    public void flush() {
        synchronized (buffer) {
            if (buffer.isEmpty()) {
                return;
            }
            List<LogMessage> batch = new ArrayList<>(buffer);
            buffer.clear();
            try {
                clickHouseLogRepository.batchInsert(batch);
                log.debug("Flushed {} log messages to ClickHouse", batch.size());
            } catch (Exception e) {
                log.error("Failed to flush logs to ClickHouse: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 采样判断。ERROR/WARN 始终保留。
     */
    private boolean shouldSample(LogMessage message) {
        String level = message.level();
        if ("ERROR".equals(level) || "WARN".equals(level)) {
            return true;
        }
        double rate = logProperties.getSamplingRate();
        if (rate >= 1.0) {
            return true;
        }
        return ThreadLocalRandom.current().nextDouble() < rate;
    }
}
