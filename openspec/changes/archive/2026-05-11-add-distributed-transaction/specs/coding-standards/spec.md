# Coding Standards — Delta

## ADDED Requirements

### Requirement: Distributed Transaction with @GlobalTransactional

Any business operation that spans multiple microservices or multiple databases SHALL use Seata `@GlobalTransactional` annotation from `io.seata.spring.annotation.GlobalTransactional`.

The annotation SHALL be placed on the initiating service method that orchestrates the cross-service call.

The annotation SHALL set `timeoutMills` (default 60000ms) and `name` for traceability.

#### Scenario: Cross-service operation with distributed transaction

- **WHEN** a service method calls two different microservices that each write to their own database
- **THEN** the method SHALL be annotated with `@GlobalTransactional`
- **AND** all participating databases SHALL have `undo_log` table

#### Scenario: Single-service operation without distributed transaction

- **WHEN** a method only accesses a single database within one service
- **THEN** `@GlobalTransactional` SHALL NOT be used
- **AND** the local `@Transactional` SHALL be sufficient
