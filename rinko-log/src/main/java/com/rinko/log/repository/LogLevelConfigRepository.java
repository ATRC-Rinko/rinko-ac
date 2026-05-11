package com.rinko.log.repository;

import com.rinko.log.entity.LogLevelConfig;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * 日志级别配置 Repository (PostgreSQL)。
 */
public interface LogLevelConfigRepository extends CrudRepository<LogLevelConfig, Long> {

    List<LogLevelConfig> findByServiceName(String serviceName);

    Optional<LogLevelConfig> findByServiceNameAndLoggerName(String serviceName, String loggerName);

    void deleteByServiceNameAndLoggerName(String serviceName, String loggerName);
}
