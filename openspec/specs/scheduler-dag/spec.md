# Scheduler DAG

## ADDED Requirements

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
