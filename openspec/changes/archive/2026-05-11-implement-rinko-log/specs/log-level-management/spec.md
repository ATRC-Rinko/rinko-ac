# Log Level Management

## ADDED Requirements

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

Table SHALL be created via Flyway migration `V1__create_log_level_configs.sql`.

#### Scenario: Service restarts and restores log levels

- **WHEN** a service starts up and queries the log level configuration
- **THEN** all persisted log level overrides for that service SHALL be restored
- **AND** Logback loggers SHALL be updated accordingly
