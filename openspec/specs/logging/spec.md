# Logging

## ADDED Requirements

### Requirement: Logback logs are sent to Kafka via KafkaLogAppender

All business modules (rinko-auth, rinko-gateway, rinko-oss, rinko-scheduler, rinko-notify) SHALL have a KAFKA appender in their `logback-spring.xml` that sends formatted log events to the `rinko-logs` Kafka topic via the shared `KafkaLogAppender` from rinko-infra.

#### Scenario: Log event is sent to Kafka

- **WHEN** a business module logs a message at INFO level or above
- **THEN** the log event SHALL be formatted as JSON by `JsonEncoder`
- **AND** the JSON payload SHALL be sent to the `rinko-logs` Kafka topic
- **AND** the log event SHALL also appear on CONSOLE and FILE appenders

#### Scenario: rinko-log does not send its own logs to Kafka

- **WHEN** rinko-log produces log messages internally
- **THEN** those messages SHALL NOT be sent to the `rinko-logs` topic
- **AND** they SHALL only appear on CONSOLE and FILE appenders

---

### Requirement: KafkaLogAppender tolerates Kafka unavailability

The system SHALL continue logging to CONSOLE and FILE appenders when Kafka is unavailable, without throwing exceptions to the calling thread.

#### Scenario: Kafka broker is down

- **WHEN** the Kafka broker is unreachable
- **THEN** the KAFKA appender SHALL silently drop the log message
- **AND** CONSOLE and FILE appenders SHALL continue to function normally

---

### Requirement: Kafka Log Message Consumption

The system SHALL consume structured JSON log messages from a Kafka topic named `rinko-logs`.

The Kafka consumer SHALL be configured with:
- Bootstrap servers from `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`
- Consumer group ID `rinko-log-consumer`
- Auto-offset reset `earliest`
- Batch listener mode enabled

Each consumed message SHALL be a JSON object conforming to the log field specification defined in `docs/spec.md`.

#### Scenario: Normal log message consumption

- **WHEN** a microservice publishes a JSON log message to Kafka topic `rinko-logs`
- **THEN** `rinko-log` SHALL consume the message within 5 seconds
- **AND** the log SHALL be written to ClickHouse `logs` table

#### Scenario: Log messages arrive from business modules via KafkaLogAppender

- **WHEN** a business module sends a `LogMessage` JSON to the `rinko-logs` topic via `KafkaLogAppender`
- **THEN** `LogKafkaConsumer` SHALL receive the message
- **AND** `LogIngestionService` SHALL buffer and flush the message to ClickHouse
- **AND** the original log level, service name, trace ID, message, and exception SHALL be preserved

#### Scenario: Kafka unavailable during startup

- **WHEN** Kafka is unreachable when `rinko-log` starts
- **THEN** the service SHALL start successfully
- **AND** log consumption SHALL retry connecting to Kafka every 30 seconds
- **AND** once Kafka recovers, consumption SHALL resume from the last committed offset

---

### Requirement: Batch Insert into ClickHouse

The system SHALL batch-insert consumed log messages into ClickHouse `logs` table.

Batch size SHALL default to 1000 messages or flush interval of 5 seconds, whichever comes first.

The ClickHouse connection SHALL be configured with:
- Host from `${CLICKHOUSE_HOST:localhost}`
- Port from `${CLICKHOUSE_PORT:8123}`
- Database from `${CLICKHOUSE_DB:rinko_log}`
- Credentials from `${CLICKHOUSE_USER:rinko}` / `${CLICKHOUSE_PASSWORD:rinko123}`

#### Scenario: Batch insert reaches size threshold

- **WHEN** 1000 log messages accumulate in the batch buffer
- **THEN** the batch SHALL be flushed to ClickHouse immediately
- **AND** all 1000 records SHALL be queryable via ClickHouse SELECT

#### Scenario: Batch insert reaches time threshold

- **WHEN** fewer than 1000 messages are in the buffer and 5 seconds have elapsed since last flush
- **THEN** the batch SHALL be flushed to ClickHouse
- **AND** incomplete batches SHALL NOT be dropped

#### Scenario: ClickHouse insert fails

- **WHEN** a batch insert to ClickHouse fails due to connection error
- **THEN** the Kafka offset SHALL NOT be committed
- **AND** the failed batch SHALL be retried
- **AND** the error SHALL be logged at ERROR level

---

### Requirement: Log Sampling Rate

The system SHALL support a configurable sampling rate via property `rinko.log.sampling-rate`.

The sampling rate SHALL be a double value between 0.0 and 1.0, default 1.0 (100% — no sampling).

Sampling SHALL only apply to `INFO`, `DEBUG`, and `TRACE` level logs. `ERROR` and `WARN` level logs SHALL always be retained regardless of sampling rate.

In addition to sampling, the system SHALL apply level threshold filtering via `log_level_configs`:
- Query `log_level_configs` for a rule matching the log's `serviceName` and `className`
- Compare the log's level ordinal against the configured threshold level ordinal
- Drop the log if its level is below the configured threshold
- Always retain ERROR and WARN level logs regardless of threshold
- Cache the configuration map and refresh every 30 seconds

#### Scenario: Log filtered by level threshold

- **WHEN** a DEBUG level log arrives from service `rinko-auth` with className `com.rinko.auth`
- **AND** `log_level_configs` has a rule `(rinko-auth, com.rinko.auth, WARN)`
- **THEN** the log SHALL be dropped (DEBUG < WARN)

#### Scenario: Log passes level threshold

- **WHEN** an ERROR level log arrives and the configured threshold is WARN
- **THEN** the log SHALL be retained (ERROR >= WARN)

#### Scenario: No level rule configured

- **WHEN** a DEBUG log arrives and no rule exists for its serviceName+className
- **THEN** the log SHALL be retained (no rule = no filtering)

#### Scenario: Sampling rate at 50%

- **WHEN** `rinko.log.sampling-rate=0.5`
- **THEN** approximately 50% of INFO/DEBUG/TRACE messages SHALL be written to ClickHouse
- **AND** all ERROR and WARN messages SHALL still be written to ClickHouse

#### Scenario: Sampling rate at 0% drops info-level logs only

- **WHEN** `rinko.log.sampling-rate=0.0`
- **THEN** no INFO/DEBUG/TRACE messages SHALL be written to ClickHouse
- **AND** all ERROR and WARN messages SHALL still be written to ClickHouse

#### Scenario: Default sampling rate

- **WHEN** `rinko.log.sampling-rate` is not configured
- **THEN** the default value 1.0 SHALL apply
- **AND** all log messages regardless of level SHALL be written to ClickHouse

---

### Requirement: ClickHouse Logs Table Schema

The ClickHouse `logs` table SHALL have the following schema:
- `timestamp` — `DateTime64(3, 'Asia/Shanghai')` — log event timestamp
- `level` — `String` — log level (INFO, WARN, ERROR, DEBUG)
- `service` — `String` — originating service name
- `traceId` — `String` — SkyWalking trace ID
- `spanId` — `String` — SkyWalking span ID
- `class` — `String` — fully qualified class name
- `message` — `String` — log message text
- `thread` — `String` — thread name
- `context` — `String` — MDC context as JSON string
- `exception` — `Nullable(String)` — exception message if error
- `exceptionClass` — `Nullable(String)` — exception class name if error

Engine SHALL be `MergeTree()` with `PARTITION BY toYYYYMMDD(timestamp)` and `ORDER BY (timestamp, service)`.

#### Scenario: Table creation on first deployment

- **WHEN** the ClickHouse migration SQL is executed
- **THEN** the `logs` table SHALL be created with all 11 fields
- **AND** queries filtering by `timestamp` range SHALL use partition pruning

---

### Requirement: Log Search API

The system SHALL provide a REST API endpoint `GET /api/v1/logs` for querying stored logs.

Query parameters SHALL support:
- `startTime` — ISO 8601 timestamp (required)
- `endTime` — ISO 8601 timestamp (required)
- `level` — log level filter (optional, e.g., `ERROR`)
- `service` — service name filter (optional)
- `traceId` — trace ID filter (optional)
- `keyword` — full-text search in `message` field (optional)
- `page` — page number, default 1, minimum 1
- `size` — page size, default 20, maximum 100

Backend query SHALL use ClickHouse SQL with prepared statements and parameter binding.

#### Scenario: Query by time range only

- **WHEN** `GET /api/v1/logs?startTime=2026-05-10T00:00:00&endTime=2026-05-10T23:59:59`
- **THEN** the response SHALL return all logs within the time range
- **AND** results SHALL be ordered by `timestamp DESC`
- **AND** pagination SHALL apply (default page=1, size=20)

#### Scenario: Query with filters

- **WHEN** `GET /api/v1/logs?startTime=...&endTime=...&level=ERROR&service=rinko-auth`
- **THEN** the response SHALL return only ERROR-level logs from rinko-auth
- **AND** logs from other services or other levels SHALL NOT appear

#### Scenario: Query by traceId

- **WHEN** `GET /api/v1/logs?startTime=...&endTime=...&traceId=abc123`
- **THEN** the response SHALL return all log entries with `traceId=abc123`
- **AND** logs SHALL span all services in the trace chain

---

### Requirement: Log Query Response Format

The query response SHALL use the `PageResponse` DTO from `rinko-infra`.

Each log entry SHALL include all fields from the ClickHouse `logs` table.

#### Scenario: Successful paginated query

- **WHEN** querying logs with `page=2&size=50`
- **THEN** the response SHALL be `{"content": [...], "totalElements": <N>, "totalPages": <N>, "page": 2, "size": 50}`
- **AND** `content` SHALL contain at most 50 log entries

---

### Requirement: Log Query Error Handling

Invalid query parameters SHALL return RFC 7807 Problem Detail responses.

The time range SHALL be limited to a maximum of 7 days. Queries exceeding this range SHALL return HTTP 400 with an appropriate message.

#### Scenario: Missing required parameters

- **WHEN** querying without `startTime` or `endTime`
- **THEN** the response SHALL have HTTP status 400
- **AND** the error body SHALL follow RFC 7807 format with title "Bad Request"

#### Scenario: Time range too large

- **WHEN** querying with startTime and endTime spanning more than 7 days
- **THEN** the response SHALL have HTTP status 400
- **AND** the error detail SHALL indicate "Time range must not exceed 7 days"

---

### Requirement: Get Current Log Levels

The system SHALL provide `GET /api/v1/logs/levels` to retrieve the current log level configuration for stored services.

The response SHALL be a JSON map of `{serviceName: {loggerName: level, ...}}`.

#### Scenario: Query all configured log levels

- **WHEN** `GET /api/v1/logs/levels` is called
- **THEN** the response SHALL return a JSON object with all known service-level log level mappings
- **AND** HTTP status SHALL be 200

---

### Requirement: Set Log Level for a Service

The system SHALL provide `PUT /api/v1/logs/levels` to change log levels for a specific logger in a specific service.

Request body:
```json
{
  "service": "rinko-auth",
  "logger": "com.rinko.auth",
  "level": "DEBUG"
}
```

Valid levels SHALL be: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`.

The change SHALL be persisted to PostgreSQL and broadcast to the target service via Spring Cloud Bus.

#### Scenario: Change log level successfully

- **WHEN** `PUT /api/v1/logs/levels` is called with `{"service": "rinko-auth", "logger": "com.rinko.auth.service", "level": "DEBUG"}`
- **THEN** the configuration SHALL be saved to PostgreSQL table `log_level_configs`
- **AND** a `RemoteApplicationEvent` SHALL be published to Spring Cloud Bus
- **AND** the target service SHALL update its Logback logger level to DEBUG
- **AND** HTTP status 200 SHALL be returned

#### Scenario: Invalid log level

- **WHEN** `PUT /api/v1/logs/levels` is called with `"level": "INVALID"`
- **THEN** HTTP status 400 SHALL be returned
- **AND** the error SHALL indicate valid level values

#### Scenario: Target service not running

- **WHEN** the target service is not currently running
- **THEN** the configuration SHALL still be saved to PostgreSQL
- **AND** the level SHALL take effect when the target service next starts up
- **AND** HTTP status 200 SHALL be returned

---

### Requirement: Reset Log Level

The system SHALL provide `DELETE /api/v1/logs/levels/{service}/{logger}` to reset a specific logger back to its default level.

The reset SHALL remove the stored configuration from PostgreSQL and broadcast the reset to the target service.

#### Scenario: Reset a previously changed log level

- **WHEN** `DELETE /api/v1/logs/levels/rinko-auth/com.rinko.auth.service` is called
- **THEN** the configuration SHALL be removed from `log_level_configs` table
- **AND** a reset event SHALL be broadcast via Spring Cloud Bus
- **AND** the logger SHALL revert to its original level
- **AND** HTTP status 204 SHALL be returned

---

### Requirement: Log Level Persistence

Log level configurations SHALL be stored in PostgreSQL table `log_level_configs` with columns:
- `id BIGINT PRIMARY KEY`
- `service_name VARCHAR(64) NOT NULL`
- `logger_name VARCHAR(256) NOT NULL`
- `log_level VARCHAR(16) NOT NULL`
- `created_at TIMESTAMP NOT NULL DEFAULT NOW()`
- `updated_at TIMESTAMP NOT NULL DEFAULT NOW()`

Unique constraint on `(service_name, logger_name)`.

Table SHALL be created during module initialization.

#### Scenario: Service restarts and restores log levels

- **WHEN** a service starts up and queries the log level configuration
- **THEN** all persisted log level overrides for that service SHALL be restored
- **AND** Logback loggers SHALL be updated accordingly
