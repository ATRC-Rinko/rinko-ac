# Configuration Standards — Delta

## ADDED Requirements

### Requirement: SkyWalking Agent Integration

All service containers SHALL load SkyWalking Java Agent at startup via `-javaagent` JVM flag.

Agent configuration SHALL be injected via environment variables:
- `SW_AGENT_NAME` — service name (e.g., `rinko-auth`)
- `SW_AGENT_COLLECTOR_BACKEND_SERVICES` — OAP gRPC address (e.g., `skywalking-oap:11800`)

The agent jar SHALL be distributed via Docker Compose shared volume from a one-time init container.

#### Scenario: Service starts with SkyWalking agent

- **WHEN** a service container starts
- **THEN** the JVM SHALL load SkyWalking Java Agent
- **AND** trace data SHALL be sent to SkyWalking OAP at port 11800
- **AND** log traceId SHALL match SkyWalking traceId

#### Scenario: SkyWalking OAP unavailable

- **WHEN** SkyWalking OAP is unreachable during startup
- **THEN** the service SHALL still start successfully
- **AND** agent SHALL retry connecting in background
- **AND** logs SHALL show traceId as "N/A" until OAP recovers
