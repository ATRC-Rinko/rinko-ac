## MODIFIED Requirements

### Requirement: Error Response — RFC 7807 Problem Details

All error responses SHALL use RFC 7807 Problem Details format via the `ProblemDetail` class from `rinko-infra`.

The error response SHALL include:
- `type` — URI reference identifying the problem type, derived from `RinkoException.errorCode` as `/errors/{kebab-case-error-code}`
- `title` — short human-readable summary (HTTP status reason phrase)
- `status` — HTTP status code
- `detail` — human-readable explanation from the exception's `errorMessage`
- `instance` — URI reference to the specific occurrence (optional, reserved for future use)
- `timestamp` — ISO 8601 instant of the error

The `ProblemDetail` SHALL be constructed by calling `toProblemDetail()` on any `RinkoException` subclass.

The Content-Type of error responses SHALL be `application/problem+json`.

Type URI mappings SHALL be:
| errorCode | type URI |
|---|---|
| `VALIDATION_ERROR` | `/errors/validation-error` |
| `UNAUTHORIZED` | `/errors/unauthorized` |
| `FORBIDDEN` | `/errors/forbidden` |
| `NOT_FOUND` | `/errors/not-found` |
| `INTERNAL_ERROR` | `/errors/internal-error` |

Unknown exceptions (not RinkoException) SHALL use `type: "about:blank"`.

#### Scenario: Validation error response

- **WHEN** a request contains invalid input
- **THEN** the response SHALL have HTTP status 400
- **AND** Content-Type SHALL be `application/problem+json`
- **AND** the response body SHALL be JSON conforming to RFC 7807
- **AND** SHALL contain fields: `type`, `title`, `status`, `detail`, `timestamp`
- **AND** `type` SHALL be `"/errors/validation-error"`

#### Scenario: Not found error response

- **WHEN** a requested resource does not exist
- **THEN** the response SHALL have HTTP status 404
- **AND** `type` in the ProblemDetail JSON SHALL be `"/errors/not-found"`
- **AND** `title` SHALL be "Not Found"

#### Scenario: Unauthorized error response from gateway

- **WHEN** the gateway rejects a request due to missing or invalid JWT
- **THEN** the response SHALL have HTTP status 401
- **AND** `type` SHALL be `"/errors/unauthorized"`
- **AND** `title` SHALL be "Unauthorized"

#### Scenario: Internal error with unknown exception

- **WHEN** an unexpected `Exception` is caught that is not a `RinkoException`
- **THEN** the response SHALL have HTTP status 500
- **AND** `type` SHALL be `"about:blank"`
- **AND** `detail` SHALL not expose internal stack trace or implementation details

---

### Requirement: Unified API Response Format

All REST API success responses SHALL return `ApiResponse<T>` from `rinko-infra` via `ApiResponse.success(data)`.

All error responses SHALL return `ProblemDetail` via `RinkoException.toProblemDetail()` — the `ApiResponse.error()` method SHALL NOT exist.

Controllers SHALL throw `RinkoException` subclasses for error conditions rather than returning error responses directly.

`RinkoException` and its subclasses SHALL be automatically handled by the global exception handler — controllers SHALL NOT catch them manually.

The global exception handlers SHALL return `ProblemDetail` instances and set `Content-Type: application/problem+json`.

Two separate `@RestControllerAdvice` handlers SHALL exist:
- `GlobalExceptionHandler` — Servlet modules
- `ReactiveGlobalExceptionHandler` — WebFlux modules

Both SHALL handle: `ValidationException`, `UnauthorizedException`, `ForbiddenException`, `NotFoundException`, `InternalException`, `MethodArgumentNotValidException` / `WebExchangeBindException`, and `Exception` (fallback).

#### Scenario: Successful response

- **WHEN** a controller returns `ApiResponse.success(someData)`
- **THEN** the response body SHALL be `{"code":200,"message":"OK","data":{...},"timestamp":"..."}`

#### Scenario: Validation exception

- **WHEN** a `ValidationException` is thrown from the service layer
- **THEN** the handler SHALL return a `ProblemDetail` with status 400 and Content-Type `application/problem+json`

#### Scenario: Controller handles invalid input by throwing exception

- **WHEN** a controller validates input and finds it invalid
- **THEN** the controller SHALL throw `ValidationException`
- **AND** SHALL NOT manually set HTTP status on the response
- **AND** SHALL NOT return `ApiResponse.error()`

---

### Requirement: Request Validation

All request DTOs SHALL use validation annotations (`@Valid`, `@NotBlank`, `@NotNull`, `@Email`, `@Size`).

Controllers SHALL use `@Valid` or `@Validated` on request body parameters.

Validation errors from `@Valid` (Spring's `MethodArgumentNotValidException` in Servlet, `WebExchangeBindException` in WebFlux) SHALL be handled by the global exception handlers and returned as RFC 7807 ProblemDetail with status 400 and type `/errors/validation-error`.

The detail message SHALL include all field-level validation errors.

#### Scenario: Submitting an invalid registration request

- **WHEN** a user submits a registration request with an empty username
- **THEN** the response SHALL have HTTP status 400
- **AND** Content-Type SHALL be `application/problem+json`
- **AND** the error detail SHALL indicate which field validation failed

#### Scenario: Multiple validation failures

- **WHEN** a request has multiple field validation errors (e.g., empty username and invalid email)
- **THEN** the response SHALL have HTTP status 400
- **AND** the ProblemDetail detail field SHALL list all failing fields and their error messages
