## MODIFIED Requirements

### Requirement: Log ingestion from Kafka

The system SHALL consume structured log messages from the `rinko-logs` Kafka topic and persist them to ClickHouse.

#### Scenario: Log messages arrive from business modules via Kafka

- **WHEN** a business module sends a `LogMessage` JSON to the `rinko-logs` topic
- **THEN** `LogKafkaConsumer` SHALL receive the message
- **AND** `LogIngestionService` SHALL buffer and flush the message to ClickHouse
- **AND** the original log level, service name, trace ID, message, and exception SHALL be preserved
