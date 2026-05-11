## 1. Maven Dependencies

- [x] 1.1 Add `spring-kafka` dependency to `rinko-log/pom.xml`
- [x] 1.2 Add `clickhouse-jdbc` dependency (classifier `http`) to `rinko-log/pom.xml`
- [x] 1.3 Add `spring-cloud-starter-bus-amqp` dependency to `rinko-log/pom.xml`
- [x] 1.4 Add `spring-boot-starter-data-jdbc` dependency to `rinko-log/pom.xml` (PostgreSQL + Flyway)

## 2. Database & Infrastructure

- [x] 2.1 Create Flyway migration `V1__create_log_level_configs.sql` — `log_level_configs` table with unique constraint on (service_name, logger_name)
- [x] 2.2 Create ClickHouse migration SQL `clickhouse/V1__create_logs_table.sql` — `logs` table with MergeTree, partition by day, ORDER BY (timestamp, service)
- [x] 2.3 Create `LogProperties.java` — `@ConfigurationProperties(prefix = "rinko.log")` with `samplingRate` (default 1.0) and ClickHouse sub-properties
- [x] 2.4 Create `ClickHouseConfig.java` — `DataSource` bean for ClickHouse JDBC connection

## 3. Repository Layer

- [x] 3.1 Create `LogEntry.java` entity to map ClickHouse query results
- [x] 3.2 Create `ClickHouseLogRepository.java` — batch insert via `JdbcTemplate`, query with dynamic filters
- [x] 3.3 Create `LogLevelConfig.java` entity (PostgreSQL)
- [x] 3.4 Create `LogLevelConfigRepository.java` extending `CrudRepository`

## 4. Service Layer

- [x] 4.1 Create `LogIngestionService.java` — batch buffer management, flush logic (1000 records or 5 second interval), sampling logic using `LogProperties.samplingRate`
- [x] 4.2 Create `LogQueryService.java` — query validation (7-day range limit), query execution, PageResponse assembly
- [x] 4.3 Create `LogLevelManagementService.java` — level validation, persistence, Bus event publishing

## 5. Kafka Consumer

- [x] 5.1 Create `LogKafkaConsumer.java` — `@KafkaListener` with batch mode, delegation to `LogIngestionService`
- [x] 5.2 Create `KafkaConsumerConfig.java` — consumer factory configuration, batch listener settings
- [x] 5.3 Create `LogMessage.java` DTO — maps incoming JSON log structure

## 6. Controller Layer

- [x] 6.1 Create `LogQueryController.java` — `GET /api/v1/logs` with all filter parameters and @Tag/@Operation annotations
- [x] 6.2 Create `LogLevelController.java` — `GET /api/v1/logs/levels`, `PUT /api/v1/logs/levels`, `DELETE /api/v1/logs/levels/{service}/{logger}`
- [x] 6.3 Create DTOs: `LogQueryRequest`, `LogLevelUpdateRequest`, `LogLevelResponse`

## 7. Application Entry Point

- [x] 7.1 Create `RinkoLogApplication.java` — `@SpringBootApplication` + `@EnableDiscoveryClient` + `@EnableDruid` + `@EnableScheduling`
- [x] 7.2 Update `rinko-log/src/main/resources/application.yml` — add `optional:nacos:rinko-log-dev.yml` to config import

## 8. Tests

- [x] 8.1 Create `LogIngestionServiceTest.java` — test batch flush logic and sampling
- [x] 8.2 Create `LogQueryServiceTest.java` — test query validation and pagination
- [x] 8.3 Create `LogLevelManagementServiceTest.java` — test level change and persistence

## 9. Spec Sync

- [x] 9.1 Sync 3 new specs to `openspec/specs/` — `log-ingestion`, `log-query`, `log-level-management`
