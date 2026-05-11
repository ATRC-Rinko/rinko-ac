# Scheduler Job

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
