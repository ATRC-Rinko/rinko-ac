# API Design Standards

This specification defines RESTful API design standards for the Rinko project. Requirements are derived from the actual API patterns in `rinko-auth` controllers and the principles in `docs/constitution.md` and `docs/spec.md`.

---

## ADDED Requirements

### Requirement: RESTful Path Conventions

All API endpoints SHALL follow RESTful conventions with versioned paths.

Path format SHALL be: `/api/v1/{resource}`

Resources SHALL use plural nouns (e.g., `/api/v1/auth/roles`, NOT `/api/v1/auth/role`).

Sub-resources SHALL be nested under their parent: `/api/v1/auth/roles/{id}/permissions`.

Actions that don't map to standard HTTP methods SHALL use verb suffixes: `/api/v1/auth/token/refresh`.

#### Scenario: Designing a role management API

- **WHEN** designing the role management endpoints
- **THEN** the collection endpoint SHALL be `GET /api/v1/auth/roles`
- **AND** the individual resource SHALL be `GET /api/v1/auth/roles/{id}`
- **AND** the permissions sub-resource SHALL be `POST /api/v1/auth/roles/{id}/permissions`

#### Scenario: Designing a non-CRUD action

- **WHEN** an endpoint doesn't map to standard CRUD (e.g., token refresh)
- **THEN** a verb suffix SHALL be used: `POST /api/v1/auth/token/refresh`

---

### Requirement: HTTP Method Usage

Standard HTTP methods SHALL map to CRUD operations:

| HTTP Method | Operation |
|-------------|-----------|
| GET | Read (single or collection) |
| POST | Create |
| PUT | Update (full replacement) |
| DELETE | Delete |

POST SHALL also be used for non-CRUD actions (login, refresh, revoke).

#### Scenario: Creating a new permission

- **WHEN** creating a new permission through the API
- **THEN** the request SHALL use `POST /api/v1/auth/permissions`
- **AND** the request body SHALL contain the permission data

#### Scenario: Deleting a role

- **WHEN** deleting a role through the API
- **THEN** the request SHALL use `DELETE /api/v1/auth/roles/{id}`
- **AND** a successful deletion SHALL return HTTP 204 No Content

---

### Requirement: Error Response — RFC 7807 Problem Details

All error responses SHALL use RFC 7807 Problem Details format via the `ProblemDetail` class from `rinko-infra`.

The error response SHALL include:
- `type` — URI reference identifying the problem type
- `title` — short human-readable summary
- `status` — HTTP status code
- `detail` — human-readable explanation
- `instance` — URI reference to the specific occurrence (optional)
- `timestamp` — ISO 8601 timestamp of the error

The `ProblemDetail` SHALL be constructed using the `ProblemDetailBuilder` with chainable builder pattern.

#### Scenario: Validation error response

- **WHEN** a request contains invalid input
- **THEN** the response SHALL have HTTP status 400
- **AND** the response body SHALL be JSON conforming to RFC 7807
- **AND** SHALL contain fields: `type`, `title`, `status`, `detail`, `timestamp`

#### Scenario: Not found error response

- **WHEN** a requested resource does not exist
- **THEN** the response SHALL have HTTP status 404
- **AND** `status` in the ProblemDetail JSON SHALL be 404
- **AND** `title` SHALL be "Not Found"

---

### Requirement: OpenAPI 3.0 Documentation

All controllers SHALL be documented with OpenAPI 3.0 annotations via SpringDoc.

Controller classes SHALL be annotated with `@Tag(name = "...")` to group endpoints.

Controller methods SHALL be annotated with:
- `@Operation(summary = "...")` describing the endpoint's purpose
- `@ApiResponses` listing possible HTTP responses

Security scheme SHALL be declared in the OpenAPI configuration via `@SecurityScheme` for Bearer JWT.

#### Scenario: Documenting a new endpoint

- **WHEN** a developer creates a new GET endpoint for listing users
- **THEN** the method SHALL have `@Operation(summary = "List all users")`
- **AND** the controller SHALL have `@Tag(name = "User Management")`
- **AND** `@ApiResponses` SHALL include at minimum 200 and 401 responses

#### Scenario: Accessing Swagger UI in development

- **WHEN** the module is running locally
- **THEN** Swagger UI SHALL be accessible at `/swagger-ui.html`
- **AND** the OpenAPI JSON SHALL be accessible at `/v3/api-docs`

---

### Requirement: Request Validation

All request DTOs SHALL use validation annotations (`@Valid`, `@NotBlank`, `@NotNull`, `@Email`, `@Size`).

Controllers SHALL use `@Valid` or `@Validated` on request body parameters.

Validation errors SHALL be returned as RFC 7807 ProblemDetail with status 400.

#### Scenario: Submitting an invalid registration request

- **WHEN** a user submits a registration request with an empty username
- **THEN** the response SHALL have HTTP status 400
- **AND** the error detail SHALL indicate which field validation failed

---

### Requirement: Response Format

Successful responses SHALL return appropriate HTTP status codes:
- 200 OK for successful GET/PUT operations
- 201 Created for successful POST operations (with Location header)
- 204 No Content for successful DELETE operations

Collection responses SHALL use `PageResponse<T>` for paginated results, containing:
- `content` — the list of items
- `totalElements` — total count
- `totalPages` — total pages

Single resource responses SHALL return the resource directly in the response body.

#### Scenario: Listing paginated roles

- **WHEN** requesting `GET /api/v1/auth/roles?page=1&size=20`
- **THEN** the response SHALL be a JSON object with `content`, `totalElements`, and `totalPages` fields
- **AND** `content` SHALL contain at most 20 role objects

#### Scenario: Creating a new resource

- **WHEN** a resource is successfully created via POST
- **THEN** the response status SHALL be 201
- **AND** the response body SHALL contain the created resource
- **AND** a Location header SHALL point to the new resource's URL
