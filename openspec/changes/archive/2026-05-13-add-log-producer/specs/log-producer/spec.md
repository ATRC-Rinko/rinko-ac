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

### Requirement: KafkaLogAppender tolerates Kafka unavailability

The system SHALL continue logging to CONSOLE and FILE appenders when Kafka is unavailable, without throwing exceptions to the calling thread.

#### Scenario: Kafka broker is down

- **WHEN** the Kafka broker is unreachable
- **THEN** the KAFKA appender SHALL silently drop the log message
- **AND** CONSOLE and FILE appenders SHALL continue to function normally
