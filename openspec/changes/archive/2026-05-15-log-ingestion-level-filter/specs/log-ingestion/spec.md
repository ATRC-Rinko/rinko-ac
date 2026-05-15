## MODIFIED Requirements

### Requirement: Log Sampling Rate

The system SHALL support a configurable sampling rate AND level threshold filtering via `log_level_configs`.

Level filtering SHALL:
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

#### Scenario: Level cache refresh

- **WHEN** an admin adds a new level rule via PUT /api/v1/logs/levels
- **THEN** the new rule SHALL take effect for ingestion within 30 seconds
