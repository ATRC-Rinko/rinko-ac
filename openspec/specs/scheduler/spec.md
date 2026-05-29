# Scheduler

## ADDED Requirements

### Requirement: Job CRUD API

The system SHALL provide REST API for managing scheduled jobs:

- `GET /api/v1/scheduler/jobs` — list all jobs
- `POST /api/v1/scheduler/jobs` — create job
- `PUT /api/v1/scheduler/jobs/{id}` — update job
- `DELETE /api/v1/scheduler/jobs/{id}` — delete job

Job fields:
- `name` — unique job name
- `type` — HTTP / SHELL / BEAN
- `cronExpression` — Quartz Cron expression
- `config` — JSON: HTTP (url, method, headers), SHELL (command), BEAN (beanName, methodName)
- `enabled` — whether the job is active

#### Scenario: Create a cron job

- **WHEN** `POST /api/v1/scheduler/jobs` with `{"name":"health-check","type":"HTTP","cronExpression":"0 */5 * * * ?","config":{"url":"http://localhost:8080/actuator/health","method":"GET"}}`
- **THEN** the job SHALL be registered in Quartz with the cron trigger
- **AND** HTTP 201 SHALL be returned

#### Scenario: Pause a job

- **WHEN** `POST /api/v1/scheduler/jobs/{id}/pause`
- **THEN** the job SHALL be paused (no more triggers)
- **AND** HTTP 200 SHALL be returned

#### Scenario: Trigger a job immediately

- **WHEN** `POST /api/v1/scheduler/jobs/{id}/trigger`
- **THEN** the job SHALL execute once immediately
- **AND** HTTP 200 SHALL be returned

---

### Requirement: Task Dependency Definition

The system SHALL support DAG dependencies between jobs via `scheduler_dependencies` table.

A job MAY depend on one or more upstream jobs. It SHALL only execute after ALL upstream dependencies have completed successfully.

#### Scenario: Define job dependency

- **WHEN** `POST /api/v1/scheduler/jobs/{jobId}/dependencies` with `{"dependsOnJobId": 100}`
- **THEN** a dependency record SHALL be created
- **AND** job {jobId} SHALL NOT execute until job 100 completes successfully

#### Scenario: List downstream jobs

- **WHEN** `GET /api/v1/scheduler/jobs/{jobId}/downstream`
- **THEN** all jobs that depend on {jobId} SHALL be returned

---

### Requirement: Auto-Trigger Downstream

When a job executes successfully, the system SHALL check for downstream jobs (jobs that depend on this job). If all upstream dependencies for a downstream job are COMPLETED, it SHALL be triggered automatically.

#### Scenario: Chain execution

- **WHEN** job A completes successfully
- **AND** job B depends on job A
- **THEN** job B SHALL be triggered automatically
- **AND** job B's execution SHALL be recorded in the history

---

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
