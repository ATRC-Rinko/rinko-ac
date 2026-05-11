# Gateway Resilience

## ADDED Requirements

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
