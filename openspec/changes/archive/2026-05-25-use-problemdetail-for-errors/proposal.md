## Why

The project's `api-design` spec already mandates RFC 7807 Problem Details for all error responses, and a `ProblemDetail` builder class exists in `rinko-infra`, but it is completely unused. Currently three inconsistent error formats coexist: `ApiResponse.error()` from exception handlers, hardcoded JSON in the gateway JWT filter, and Spring's default error format. The `errorCode` field from `RinkoException` (e.g., `"VALIDATION_ERROR"`, `"NOT_FOUND"`) is discarded and never reaches the client. This change aligns the implementation with the existing specification.

## What Changes

- **Replace `ApiResponse.error()` in global exception handlers** with `ProblemDetail` responses using `application/problem+json` content type
- **Wire `RinkoException.errorCode`** into ProblemDetail's `type` field so machine-readable error codes are transmitted to clients
- **Unify gateway JWT filter error format** to use the same `ProblemDetail` builder instead of hardcoded JSON strings
- **Replace inline `ApiResponse.error()` calls in controllers** with throwing appropriate `RinkoException` subclasses, letting the global handlers render them consistently
- **Add `@Valid`/`@Validated` validation error handling** to both global exception handlers (currently missing), rendering validation failures as ProblemDetail
- **Remove `ApiResponse.error()` static factory method** — `ApiResponse` becomes success-only; error paths use ProblemDetail exclusively
- **BREAKING**: Error response format changes from `{"code":400,"message":"...","data":null,"timestamp":"..."}` to `{"type":"...","title":"...","status":400,"detail":"...","timestamp":"...","instance":"..."}` (RFC 7807)

## Capabilities

### New Capabilities
- `error-problemdetail`: Standardized RFC 7807 error responses across all modules using ProblemDetail, including error code mapping, validation error handling, and unified exception handler behavior

### Modified Capabilities
- `api-design`: The existing requirement for RFC 7807 Problem Details error responses remains unchanged; this change implements what the spec already requires, with the addition of validation error handling in exception handlers and errorCode wiring

## Impact

- **`rinko-infra`**: `ApiResponse` (remove `error()` method), `ProblemDetail` (enhance with static factory from `RinkoException`), `GlobalExceptionHandler`, `ReactiveGlobalExceptionHandler` (rewrite to return ProblemDetail), add `@Valid` validation handling
- **`rinko-gateway`**: `JwtAuthFilter` — use ProblemDetail builder instead of hardcoded JSON
- **`rinko-auth`**: Controllers with inline `ApiResponse.error()` calls — replace with `ValidationException` throws
- **`rinko-oss`, `rinko-log`, `rinko-notify`, `rinko-scheduler`**: All controllers that call `ApiResponse.error()` directly — replace with appropriate exception throws
- **Clients** consuming the API: Must update to parse RFC 7807 format instead of the legacy `ApiResponse.error()` format
