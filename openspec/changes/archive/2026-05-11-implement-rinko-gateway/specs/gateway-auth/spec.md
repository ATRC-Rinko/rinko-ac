# Gateway Authentication

## ADDED Requirements

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
