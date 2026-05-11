package com.rinko.log.service;

import com.rinko.infra.exception.NotFoundException;
import com.rinko.infra.exception.ValidationException;
import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.log.entity.LogLevelConfig;
import com.rinko.log.repository.LogLevelConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 动态日志级别管理服务。
 */
@Service
public class LogLevelManagementService {

    private static final Logger log = LoggerFactory.getLogger(LogLevelManagementService.class);
    private static final Set<String> VALID_LEVELS = Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR");

    private final LogLevelConfigRepository logLevelConfigRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final BusProperties busProperties;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public LogLevelManagementService(LogLevelConfigRepository logLevelConfigRepository,
                                       ApplicationEventPublisher eventPublisher,
                                       BusProperties busProperties) {
        this.logLevelConfigRepository = logLevelConfigRepository;
        this.eventPublisher = eventPublisher;
        this.busProperties = busProperties;
    }

    /**
     * 获取所有日志级别配置。
     */
    public Iterable<LogLevelConfig> getAllConfigs() {
        return logLevelConfigRepository.findAll();
    }

    /**
     * 修改日志级别。
     */
    @Transactional
    public LogLevelConfig setLogLevel(String service, String loggerName, String level) {
        if (!VALID_LEVELS.contains(level)) {
            throw new ValidationException("Invalid log level: " + level + ". Valid: " + VALID_LEVELS);
        }

        LogLevelConfig config = logLevelConfigRepository
                .findByServiceNameAndLoggerName(service, loggerName)
                .orElseGet(() -> {
                    LogLevelConfig newConfig = new LogLevelConfig();
                    newConfig.setId(idGenerator.nextId());
                    newConfig.setServiceName(service);
                    newConfig.setLoggerName(loggerName);
                    newConfig.setCreatedAt(LocalDateTime.now());
                    return newConfig;
                });

        config.setLogLevel(level);
        config.setUpdatedAt(LocalDateTime.now());
        LogLevelConfig saved = logLevelConfigRepository.save(config);

        publishEvent(service, loggerName, level);

        log.info("Log level changed: service={}, logger={}, level={}", service, loggerName, level);
        return saved;
    }

    /**
     * 重置日志级别。
     */
    @Transactional
    public void resetLogLevel(String service, String loggerName) {
        LogLevelConfig config = logLevelConfigRepository
                .findByServiceNameAndLoggerName(service, loggerName)
                .orElseThrow(() -> new NotFoundException("Log level config not found for " + service + "/" + loggerName));
        logLevelConfigRepository.delete(config);
        publishEvent(service, loggerName, null);
        log.info("Log level reset: service={}, logger={}", service, loggerName);
    }

    private void publishEvent(String targetService, String loggerName, String level) {
        LogLevelChangeEvent event = new LogLevelChangeEvent(
                this, busProperties.getId(), targetService, loggerName, level);
        eventPublisher.publishEvent(event);
    }

    /**
     * 通过 Spring Cloud Bus 发布的日志级别变更事件。
     */
    public static class LogLevelChangeEvent extends RemoteApplicationEvent {
        private final String loggerName;
        private final String logLevel;

        public LogLevelChangeEvent() {
            this.loggerName = null;
            this.logLevel = null;
        }

        public LogLevelChangeEvent(Object source, String originService,
                                     String destinationService, String loggerName, String logLevel) {
            super(source, originService, destinationService);
            this.loggerName = loggerName;
            this.logLevel = logLevel;
        }

        public String getLoggerName() { return loggerName; }
        public String getLogLevel() { return logLevel; }
    }
}
