# Distributed Transaction

## ADDED Requirements

### Requirement: Seata AT Mode Distributed Transaction

All business modules SHALL support Seata AT mode distributed transactions via `@GlobalTransactional` annotation.

The `spring-cloud-starter-alibaba-seata` dependency SHALL be present in all service modules (except `rinko-infra`).

Each module's PostgreSQL database SHALL have the `undo_log` table for Seata automatic rollback.

#### Scenario: Cross-service transaction succeeds

- **WHEN** a method annotated with `@GlobalTransactional` calls rinko-auth and rinko-notify
- **THEN** both operations SHALL commit if all succeed
- **AND** Seata SHALL register branch transactions for each service

#### Scenario: Cross-service transaction rolls back

- **WHEN** a `@GlobalTransactional` method calls rinko-auth (success) then rinko-notify (throws exception)
- **THEN** rinko-auth's changes SHALL be automatically rolled back via undo_log
- **AND** rinko-notify's partial changes SHALL be discarded

---

### Requirement: Seata Server Configuration

Seata Server (TC) SHALL be deployed via Docker Compose with:
- Image: `seataio/seata-server:2.2.0`
- Registry: Nacos (`127.0.0.1:18848`)
- Config: Nacos
- Store mode: `db` (PostgreSQL)

Transaction service group SHALL be `rinko-tx-group`.

#### Scenario: Seata Server registers with Nacos

- **WHEN** `docker compose up seata-server` starts
- **THEN** the Seata Server SHALL register in Nacos
- **AND** business modules SHALL discover it via `rinko-tx-group`

---

### Requirement: undo_log Table

Each module's database SHALL have `undo_log` table created via Flyway migration.

The `undo_log` table SHALL follow Seata AT mode schema with columns: `id`, `branch_id`, `xid`, `context`, `rollback_info`, `log_status`, `log_created`, `log_modified`.

#### Scenario: undo_log records created during transaction

- **WHEN** a `@GlobalTransactional` method modifies data
- **THEN** Seata SHALL insert before-image into `undo_log` before each UPDATE
- **AND** undo_log records SHALL be cleaned up after successful commit
