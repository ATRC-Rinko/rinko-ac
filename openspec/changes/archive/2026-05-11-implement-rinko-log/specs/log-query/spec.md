# Log Query

## ADDED Requirements

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
