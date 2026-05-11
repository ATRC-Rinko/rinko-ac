# Scheduler Execution

## ADDED Requirements

### Requirement: Execution History

Every job execution SHALL be recorded in `scheduler_executions` table:
- `id BIGINT PRIMARY KEY`
- `job_id BIGINT`
- `status VARCHAR(16)` — RUNNING / SUCCESS / FAILED
- `start_time TIMESTAMP`
- `end_time TIMESTAMP`
- `retry_count INT DEFAULT 0`
- `result TEXT` — execution output or error message

#### Scenario: Job executed and recorded

- **WHEN** a job executes
- **THEN** an execution record SHALL be created with status RUNNING
- **AND** on completion, status SHALL be updated to SUCCESS or FAILED
- **AND** `end_time` SHALL be set

---

### Requirement: Query Execution History

The system SHALL provide `GET /api/v1/scheduler/executions?jobId={id}&page={page}&size={size}` for querying execution history.

#### Scenario: Query recent executions

- **WHEN** `GET /api/v1/scheduler/executions?jobId=1&page=1&size=20`
- **THEN** the 20 most recent executions of job 1 SHALL be returned
- **AND** response SHALL be `ApiResponse<PageResponse<SchedulerExecution>>`

---

### Requirement: Retry on Failure

When a job fails, the system SHALL retry up to the configured maximum (default 3).

Retry delay SHALL use exponential backoff: 1s → 4s → 9s.

If all retries fail, status SHALL be FAILED.

#### Scenario: Job fails and retries

- **WHEN** a job fails on first attempt
- **THEN** it SHALL be retried after 1 second
- **AND** if it fails again, SHALL be retried after 4 seconds
- **AND** after 3 failures, status SHALL be FAILED

---

### Requirement: Failure Alert

When a job exhausts all retries, the system SHALL send a RabbitMQ message to `notify.queue` with job name, failure reason, and timestamp. rinko-notify SHALL handle the alert delivery.

#### Scenario: Failed job sends alert

- **WHEN** a job reaches max retries and fails
- **THEN** a message SHALL be published to RabbitMQ `notify.queue`
- **AND** the message SHALL include `{"type":"JOB_FAILED","jobName":"...","error":"...","timestamp":"..."}`
