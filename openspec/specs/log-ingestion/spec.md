# Log Ingestion

## ADDED Requirements

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
