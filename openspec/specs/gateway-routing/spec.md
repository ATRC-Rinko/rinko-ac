# Gateway Routing

## ADDED Requirements

### Requirement: Service Route Configuration

The gateway SHALL route external requests to backend microservices via Spring Cloud Gateway routes configured in Nacos.

Five routes SHALL be defined:

| Route ID | Path Pattern | Target |
|----------|-------------|--------|
| `auth-service` | `/api/v1/auth/**` | `lb://rinko-auth` |
| `oss-service` | `/api/v1/oss/**` | `lb://rinko-oss` |
| `log-service` | `/api/v1/logs/**` | `lb://rinko-log` |
| `notify-service` | `/api/v1/notify/**` | `lb://rinko-notify` |
| `scheduler-service` | `/api/v1/scheduler/**` | `lb://rinko-scheduler` |

All routes SHALL use `lb://` (load-balanced) URIs for Nacos service discovery.

#### Scenario: Request to auth service via gateway

- **WHEN** `GET /api/v1/auth/permissions` arrives at the gateway
- **THEN** the request SHALL be routed to `lb://rinko-auth`
- **AND** Nacos SHALL resolve the available rinko-auth instance(s)

#### Scenario: Request to non-existent path

- **WHEN** `GET /api/v1/unknown/endpoint` arrives at the gateway
- **THEN** the gateway SHALL return HTTP 404
- **AND** the response body SHALL follow RFC 7807 format

---

### Requirement: Health Check Endpoint

The gateway SHALL expose `/actuator/health` for health check, accessible without authentication.

#### Scenario: Health check request

- **WHEN** `GET /actuator/health` is called
- **THEN** the response SHALL be HTTP 200 with `{"status": "UP"}`
- **AND** no JWT token SHALL be required

---

### Requirement: Swagger/OpenAPI Proxy

The gateway SHALL proxy Swagger UI and OpenAPI doc requests to the appropriate backend service:
- `/v3/api-docs/**` → backend services
- `/swagger-ui/**` → backend services (via webjars or proxying)

These paths SHALL be in the authentication whitelist.

#### Scenario: Accessing Swagger UI

- **WHEN** `GET /swagger-ui.html` is requested
- **THEN** the gateway SHALL route to the backend service
- **AND** no authentication SHALL be required
