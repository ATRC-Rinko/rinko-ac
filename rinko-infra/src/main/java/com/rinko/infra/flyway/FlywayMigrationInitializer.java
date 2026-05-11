package com.rinko.infra.flyway;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;

/**
 * Flyway 数据库迁移自动初始化器。
 * 在应用启动就绪后自动执行 Flyway 迁移。
 */
@AutoConfiguration
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "rinko.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FlywayMigrationInitializer {

    private static final Logger log = LoggerFactory.getLogger(FlywayMigrationInitializer.class);

    private final DataSource dataSource;

    public FlywayMigrationInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrate() {
        log.info("Starting Flyway database migration...");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();
        flyway.migrate();
        log.info("Flyway database migration completed successfully.");
    }
}
