package com.rinko.log.service;

import com.rinko.log.config.LogProperties;
import com.rinko.log.dto.LogMessage;
import com.rinko.log.model.entity.LogLevelConfig;
import com.rinko.log.repository.ClickHouseLogRepository;
import com.rinko.log.repository.LogLevelConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 日志摄入服务：批量缓冲 + 定时冲刷 + 采样 + 级别过滤。
 */
@Service
public class LogIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LogIngestionService.class);
    private static final Map<String, Integer> LEVEL_ORDINAL = Map.of(
            "TRACE", 0, "DEBUG", 1, "INFO", 2, "WARN", 3, "ERROR", 4);
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private static final int MAX_BATCH_SIZE = 1000;

    private final ClickHouseLogRepository clickHouseLogRepository;
    private final LogProperties logProperties;
    private final LogLevelConfigMapper logLevelConfigMapper;
    private final BlockingQueue<LogMessage> buffer = new LinkedBlockingQueue<>(10000);

    private volatile Map<String, String> levelCache = new ConcurrentHashMap<>();

    public LogIngestionService(ClickHouseLogRepository clickHouseLogRepository,
                               LogProperties logProperties,
                               LogLevelConfigMapper logLevelConfigMapper) {
        this.clickHouseLogRepository = clickHouseLogRepository;
        this.logProperties = logProperties;
        this.logLevelConfigMapper = logLevelConfigMapper;
    }

    /**
     * 接收一条日志消息。先级别过滤，再采样，WARN/ERROR 始终保留。
     */
    public void ingest(String message) {
        LogMessage logMessage = readLogMessage(message);
        if (!shouldKeepByLevel(logMessage)) {
            return;
        }
        if (!shouldSample(logMessage)) {
            return;
        }
        buffer.add(logMessage);
        if (buffer.size() >= 100) {
            flush();
        }
    }

    /**
     * 批量接收日志消息。
     */
    public void ingestBatch(List<String> messages) {
        for (String msg : messages) {
            LogMessage logMessage = readLogMessage(msg);
            if (shouldKeepByLevel(logMessage) && shouldSample(logMessage)) {
                buffer.add(logMessage);
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
        List<LogMessage> batch = new ArrayList<>();
        buffer.drainTo(batch, MAX_BATCH_SIZE);
        if (batch.isEmpty()) {
            return;
        }
        try {
            clickHouseLogRepository.batchInsert(batch);
            log.debug("Flushed {} log messages to ClickHouse", batch.size());
        } catch (Exception e) {
            log.error("Failed to flush logs to ClickHouse: {}", e.getMessage(), e);
        }
    }

    /**
     * 定时刷新级别配置缓存（每 30 秒）。
     */
    @Scheduled(fixedRate = 30000)
    public void refreshLevelCache() {
        doRefreshLevelCache();
    }

    /** 监听日志级别变更事件，立即刷新缓存（无需等待轮询）。 */
    @EventListener
    public void onLogLevelChange(LogLevelManagementService.LogLevelChangeEvent event) {
        log.debug("Log level change detected via event, refreshing cache immediately");
        doRefreshLevelCache();
    }

    private void doRefreshLevelCache() {
        try {
            List<LogLevelConfig> configs = logLevelConfigMapper.selectList(null);
            Map<String, String> newCache = new ConcurrentHashMap<>();
            for (LogLevelConfig c : configs) {
                newCache.put(c.getServiceName() + ":" + c.getLoggerName(), c.getLogLevel());
            }
            this.levelCache = newCache;
        } catch (Exception e) {
            log.warn("Failed to refresh level cache: {}", e.getMessage());
        }
    }

    /**
     * 级别过滤：低于配置阈值的日志丢弃。ERROR/WARN 始终保留。
     */
    private boolean shouldKeepByLevel(LogMessage message) {
        String level = message.level();
        if ("ERROR".equals(level) || "WARN".equals(level)) {
            return true;
        }
        String threshold = levelCache.get(message.service() + ":" + message.className());
        if (threshold == null) {
            return true; // 无配置则保留
        }
        Integer msgOrdinal = LEVEL_ORDINAL.get(level);
        Integer thresholdOrdinal = LEVEL_ORDINAL.get(threshold);
        if (msgOrdinal == null || thresholdOrdinal == null) {
            return true;
        }
        return msgOrdinal >= thresholdOrdinal;
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

    private LogMessage readLogMessage(String message) {
        try {
            return JSON_MAPPER.readValue(message, LogMessage.class);
        } catch (Exception e) {
            log.warn("Failed to parse log message: {}", e.getMessage());
            return null;
        }
    }
}
