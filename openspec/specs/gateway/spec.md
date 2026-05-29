# Gateway

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

---

### Requirement: JWT Token Validation GlobalFilter

The gateway SHALL validate JWT Bearer tokens on every request via a `GlobalFilter`.

The filter SHALL:
1. Extract the token from the `Authorization: Bearer <token>` header
2. Parse and validate the JWT using HMAC-SHA key (same secret as rinko-auth)
3. Verify token signature and expiration
4. Extract `userId` and `roles` from JWT claims
5. Inject `X-User-Id` and `X-User-Roles` headers into the proxied request

If the token is missing, expired, or invalid, the gateway SHALL return HTTP 401.

#### Scenario: Valid token passes through gateway

- **WHEN** a request arrives with `Authorization: Bearer <valid-jwt>`
- **THEN** the gateway SHALL validate the token locally
- **AND** inject `X-User-Id: 123` and `X-User-Roles: admin,user` headers
- **AND** forward the request to the backend service

#### Scenario: No token provided

- **WHEN** a request arrives without an Authorization header
- **THEN** the gateway SHALL return HTTP 401
- **AND** the response SHALL be RFC 7807 with title "Unauthorized"

#### Scenario: Expired token

- **WHEN** a request arrives with an expired JWT
- **THEN** the gateway SHALL return HTTP 401
- **AND** the response SHALL indicate "Token expired"

#### Scenario: Invalid token signature

- **WHEN** a request arrives with a JWT signed with a different secret
- **THEN** the gateway SHALL return HTTP 401
- **AND** the response SHALL NOT reveal the secret or validation details

---

### Requirement: Authentication Whitelist

The gateway SHALL skip JWT validation for the following paths:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/token/refresh`
- `/oauth2/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/actuator/health`

#### Scenario: Registration request bypasses auth

- **WHEN** `POST /api/v1/auth/register` arrives without a JWT token
- **THEN** the request SHALL pass through the gateway to rinko-auth
- **AND** no authentication error SHALL be returned

#### Scenario: Protected endpoint requires auth

- **WHEN** `GET /api/v1/auth/users` arrives without a JWT token
- **THEN** the gateway SHALL return HTTP 401
- **AND** the request SHALL NOT reach the backend service

---

### Requirement: Sentinel Rate Limiting

The gateway SHALL integrate Sentinel for route-level QPS limiting.

Default rate limit SHALL be 100 QPS per route.

When a route exceeds its QPS limit, the gateway SHALL return HTTP 429 Too Many Requests with an RFC 7807 Problem Detail body.

#### Scenario: Normal traffic within limit

- **WHEN** a route receives traffic below its QPS limit
- **THEN** all requests SHALL be forwarded to the backend service normally

#### Scenario: Traffic exceeds QPS limit

- **WHEN** a route receives traffic exceeding its QPS limit
- **THEN** excess requests SHALL be rejected with HTTP 429
- **AND** the response body SHALL be RFC 7807 with title "Too Many Requests"

---

### Requirement: Sentinel Circuit Breaking

The gateway SHALL configure Sentinel circuit breaking for backend services that become unavailable.

When a backend service fails continuously, Sentinel SHALL open the circuit and fast-fail requests with HTTP 503 Service Unavailable.

The circuit SHALL half-open after a cooling period (default 30 seconds) to test if the service has recovered.

#### Scenario: Backend service is down

- **WHEN** rinko-auth is unreachable
- **THEN** requests to `/api/v1/auth/**` SHALL fail with HTTP 503 after circuit opens
- **AND** the response SHALL indicate "Service Unavailable"
- **AND** the circuit SHALL half-open after the cooling period

#### Scenario: Backend service recovers

- **WHEN** rinko-auth becomes reachable again during half-open state
- **THEN** the circuit SHALL close
- **AND** requests SHALL resume normal forwarding

---

### Requirement: CORS Configuration

The gateway SHALL handle CORS preflight requests globally.

Allowed origins, methods, and headers SHALL be configurable via `rinko.cors` Nacos properties (inherited from `rinko-infra`).

CORS SHALL be processed before JWT authentication.

#### Scenario: CORS preflight request

- **WHEN** an `OPTIONS` request arrives with `Origin` and `Access-Control-Request-Method` headers
- **THEN** the gateway SHALL respond with appropriate CORS headers
- **AND** no JWT authentication SHALL be required
- **AND** the response SHALL include `Access-Control-Allow-Origin`
