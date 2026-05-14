package com.rinko.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rinko.infra.exception.NotFoundException;
import com.rinko.infra.exception.ValidationException;
import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.log.entity.LogLevelConfig;
import com.rinko.log.repository.LogLevelConfigMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.cloud.bus.BusProperties;
//import org.springframework.cloud.bus.event.RemoteApplicationEvent;
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
@RequiredArgsConstructor
public class LogLevelManagementService {

    private static final Logger log = LoggerFactory.getLogger(LogLevelManagementService.class);
    private static final Set<String> VALID_LEVELS = Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR");

    private final LogLevelConfigMapper logLevelConfigMapper;
    private final ApplicationEventPublisher eventPublisher;
//    private final BusProperties busProperties;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 获取所有日志级别配置。
     */
    public List<LogLevelConfig> getAllConfigs() {
        return logLevelConfigMapper.selectList(null);
    }

    /**
     * 修改日志级别。
     */
    @Transactional
    public LogLevelConfig setLogLevel(String service, String loggerName, String level) {
        if (!VALID_LEVELS.contains(level)) {
            throw new ValidationException("Invalid log level: " + level + ". Valid: " + VALID_LEVELS);
        }

        LogLevelConfig config = logLevelConfigMapper.selectOne(
                new LambdaQueryWrapper<LogLevelConfig>()
                        .eq(LogLevelConfig::getServiceName, service)
                        .eq(LogLevelConfig::getLoggerName, loggerName));

        if (config == null) {
            config = new LogLevelConfig();
            config.setId(idGenerator.nextId());
            config.setServiceName(service);
            config.setLoggerName(loggerName);
            config.setCreatedAt(LocalDateTime.now());
            config.setLogLevel(level);
            config.setUpdatedAt(LocalDateTime.now());
            logLevelConfigMapper.insert(config);
        } else {
            config.setLogLevel(level);
            config.setUpdatedAt(LocalDateTime.now());
            logLevelConfigMapper.updateById(config);
        }

//        publishEvent(service, loggerName, level);

        log.info("Log level changed: service={}, logger={}, level={}", service, loggerName, level);
        return config;
    }

    /**
     * 重置日志级别。
     */
    @Transactional
    public void resetLogLevel(String service, String loggerName) {
        LogLevelConfig config = logLevelConfigMapper.selectOne(
                new LambdaQueryWrapper<LogLevelConfig>()
                        .eq(LogLevelConfig::getServiceName, service)
                        .eq(LogLevelConfig::getLoggerName, loggerName));

        if (config == null) {
            throw new NotFoundException("Log level config not found for " + service + "/" + loggerName);
        }

        logLevelConfigMapper.deleteById(config.getId());
//        publishEvent(service, loggerName, null);
        log.info("Log level reset: service={}, logger={}", service, loggerName);
    }

//    private void publishEvent(String targetService, String loggerName, String level) {
//        LogLevelChangeEvent event = new LogLevelChangeEvent(
//                this, busProperties.getId(), targetService, loggerName, level);
//        eventPublisher.publishEvent(event);
//    }

//    /**
//     * 通过 Spring Cloud Bus 发布的日志级别变更事件。
//     */
//    public static class LogLevelChangeEvent extends RemoteApplicationEvent {
//        private final String loggerName;
//        private final String logLevel;
//
//        public LogLevelChangeEvent() {
//            this.loggerName = null;
//            this.logLevel = null;
//        }
//
//        public LogLevelChangeEvent(Object source, String originService,
//                                     String destinationService, String loggerName, String logLevel) {
//            super(source, originService, destinationService);
//            this.loggerName = loggerName;
//            this.logLevel = logLevel;
//        }
//
//        public String getLoggerName() { return loggerName; }
//        public String getLogLevel() { return logLevel; }
//    }
}
