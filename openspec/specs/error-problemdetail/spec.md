# Error Response — ProblemDetail (RFC 7807)

This specification defines the standardized RFC 7807 Problem Details error response format for all Rinko modules.

---

## ADDED Requirements

### Requirement: RinkoException to ProblemDetail Mapping

Every `RinkoException` subclass SHALL provide a `toProblemDetail()` method that produces an RFC 7807 compliant `ProblemDetail` instance.

The mapping SHALL use the exception's `errorCode`, `errorMessage`, and `httpStatus` fields.

The `type` field SHALL be a relative URI path derived from the error code: `/errors/{errorcode-lowercase-hyphenated}`.

The `title` field SHALL be the HTTP status reason phrase (e.g., "Bad Request", "Not Found").

The `detail` field SHALL be the exception's `errorMessage`.

The `status` field SHALL be the HTTP status code integer value.

The `timestamp` field SHALL be set to `Instant.now()` automatically by the ProblemDetail builder.

#### Scenario: ValidationException produces ProblemDetail

- **WHEN** a `ValidationException` is thrown with message "username is required"
- **THEN** `toProblemDetail().getType()` SHALL be `"/errors/validation-error"`
- **AND** `toProblemDetail().getTitle()` SHALL be `"Bad Request"`
- **AND** `toProblemDetail().getStatus()` SHALL be `400`
- **AND** `toProblemDetail().getDetail()` SHALL be `"username is required"`

#### Scenario: NotFoundException produces ProblemDetail

- **WHEN** a `NotFoundException` is thrown with message "File not found: abc123"
- **THEN** `toProblemDetail().getType()` SHALL be `"/errors/not-found"`
- **AND** `toProblemDetail().getTitle()` SHALL be `"Not Found"`
- **AND** `toProblemDetail().getStatus()` SHALL be `404`

#### Scenario: Generic Exception fallback

- **WHEN** an unknown `Exception` (not a `RinkoException`) is caught by the global handler
- **THEN** the ProblemDetail SHALL have `type` = `"about:blank"`
- **AND** `title` SHALL be `"Internal Server Error"`
- **AND** `status` SHALL be `500`
- **AND** `detail` SHALL be a generic message not exposing internal details

---

### Requirement: ProblemDetail Response Content-Type

All error responses SHALL be returned with `Content-Type: application/problem+json`.

#### Scenario: Validation error response headers

- **WHEN** the global exception handler processes a `ValidationException`
- **THEN** the HTTP response SHALL include header `Content-Type: application/problem+json`

#### Scenario: Gateway authentication failure response headers

- **WHEN** the gateway JWT filter rejects an unauthenticated request
- **THEN** the HTTP response SHALL include header `Content-Type: application/problem+json`

---

### Requirement: Global Exception Handler Returns ProblemDetail

Both `GlobalExceptionHandler` (Servlet) and `ReactiveGlobalExceptionHandler` (WebFlux) SHALL return `ProblemDetail` instances instead of `ApiResponse` for all error handling methods.

Each handler method SHALL call `ex.toProblemDetail()` for `RinkoException` subclasses.

Each handler method SHALL set the response status via `HttpServletResponse.setStatus()` (Servlet) or `ServerWebExchange.response.statusCode` (Reactive).

#### Scenario: Servlet handler catches NotFoundException

- **WHEN** a `NotFoundException` is thrown in a Servlet module
- **THEN** `GlobalExceptionHandler.handleNotFound()` SHALL set response status to 404
- **AND** return a `ProblemDetail` with `type="/errors/not-found"`, `status=404`

#### Scenario: Reactive handler catches ValidationException

- **WHEN** a `ValidationException` is thrown in a WebFlux module
- **THEN** `ReactiveGlobalExceptionHandler.handleValidation()` SHALL set response status to 400
- **AND** return a `Mono.just(ProblemDetail)` with `type="/errors/validation-error"`, `status=400`

---

### Requirement: @Valid Validation Error Handling

Both global exception handlers SHALL handle `MethodArgumentNotValidException` (Servlet) and `WebExchangeBindException` (Reactive) to return RFC 7807 ProblemDetail with status 400.

The detail message SHALL include all field-level validation errors, concatenated with field name and error message.

#### Scenario: Servlet @Valid validation failure

- **WHEN** a request body fails `@Valid` validation with field error "email must be a valid email"
- **THEN** the response status SHALL be 400
- **AND** the ProblemDetail SHALL have `type="/errors/validation-error"`
- **AND** `detail` SHALL contain "email: must be a valid email"

#### Scenario: Reactive @Valid validation failure

- **WHEN** a request body fails `@Valid` validation in a WebFlux controller
- **THEN** the response status SHALL be 400
- **AND** the ProblemDetail SHALL have `type="/errors/validation-error"`

---

### Requirement: Gateway JWT Filter Uses Shared ProblemDetail

The `JwtAuthFilter` in `rinko-gateway` SHALL use the `ProblemDetail` builder from `rinko-infra` instead of constructing JSON strings inline.

The ProblemDetail instance SHALL be serialized to JSON using Jackson's `ObjectMapper`.

#### Scenario: Gateway rejects request with missing Authorization header

- **WHEN** a request to a protected path has no Authorization header
- **THEN** the response SHALL have status 401
- **AND** Content-Type SHALL be `application/problem+json`
- **AND** the body SHALL be ProblemDetail JSON with `type="/errors/unauthorized"`, `title="Unauthorized"`, `status=401`

#### Scenario: Gateway rejects request with expired token

- **WHEN** a request to a protected path has an expired JWT
- **THEN** the response SHALL have status 401
- **AND** the body SHALL be ProblemDetail JSON with `type="/errors/unauthorized"`, `title="Unauthorized"`

---

### Requirement: ApiResponse Success-Only

`ApiResponse<T>` SHALL only have `success()` factory methods. The `error(int, String)` factory method SHALL be removed.

All controllers SHALL use `ApiResponse.success()` for 2xx responses and throw `RinkoException` subclasses for errors.

Controllers SHALL NOT manually set HTTP status codes before throwing exceptions — the global handler sets the correct status.

#### Scenario: Controller returns success response

- **WHEN** a controller returns `ApiResponse.success(data)`
- **THEN** the response SHALL be `{"code":200,"message":"OK","data":{...},"timestamp":"..."}`

#### Scenario: Controller handles invalid input

- **WHEN** a controller validates input and finds it invalid
- **THEN** the controller SHALL throw `ValidationException` with a descriptive message
- **AND** SHALL NOT return `ApiResponse.error()`
