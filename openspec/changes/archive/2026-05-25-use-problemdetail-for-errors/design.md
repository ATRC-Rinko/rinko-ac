## Context

The project already defines:
- `ProblemDetail` class in `rinko-infra` (RFC 7807 compliant, builder pattern, with `type`, `title`, `status`, `detail`, `instance`, `timestamp`, `extensions`)
- `RinkoException` base class with `errorCode` (machine-readable), `errorMessage` (human-readable), `httpStatus`
- Two global exception handlers (`GlobalExceptionHandler` for Servlet, `ReactiveGlobalExceptionHandler` for WebFlux)
- A gateway JWT filter that hardcodes a problem+json response

However, the exception handlers return `ApiResponse.error()` (a legacy format: `{code, message, data, timestamp}`) instead of ProblemDetail, and the `errorCode` field from exceptions is discarded. The gateway JWT filter also constructs its own JSON string rather than using the shared `ProblemDetail` builder.

This change reconciles the implementation with the existing `api-design` spec requirement for RFC 7807 error responses.

## Goals / Non-Goals

**Goals:**
- All error responses use `ProblemDetail` with `Content-Type: application/problem+json`
- `RinkoException.errorCode` is preserved in the response as ProblemDetail's `type` field
- Gateway JWT filter uses the shared `ProblemDetail` builder instead of hardcoded JSON
- Inline `ApiResponse.error()` calls in controllers are replaced with proper exception throws
- Servlet and Reactive modules share the same ProblemDetail rendering logic
- `ApiResponse` becomes success-only (remove `error()` factory)

**Non-Goals:**
- Changing the success response format (`ApiResponse.success()` remains)
- Adding i18n/localization for error messages (future consideration)
- Modifying the exception class hierarchy (classes and their error codes stay the same)
- Adding error logging/monitoring infrastructure beyond what exists

## Decisions

### Decision 1: Type URI format

Each exception subclass maps to a fixed `type` URI derived from its `errorCode`:

| Exception | errorCode | type URI |
|---|---|---|
| ValidationException | `VALIDATION_ERROR` | `/errors/validation-error` |
| UnauthorizedException | `UNAUTHORIZED` | `/errors/unauthorized` |
| ForbiddenException | `FORBIDDEN` | `/errors/forbidden` |
| NotFoundException | `NOT_FOUND` | `/errors/not-found` |
| InternalException | `INTERNAL_ERROR` | `/errors/internal-error` |
| Unknown Exception | N/A | `about:blank` |

**Rationale**: Uses relative URIs consistent with the project's API structure. The `about:blank` fallback follows RFC 7807 for truly unknown errors. No need for absolute URLs since the project doesn't host a public error documentation site.

**Alternative considered**: Absolute URLs like `https://api.rinko.com/errors/validation-error`. Rejected because the base URL varies by environment and adds unnecessary configuration complexity.

### Decision 2: ProblemDetail static factory on RinkoException

Add a `toProblemDetail()` method on `RinkoException` itself rather than on ProblemDetail. This keeps the mapping logic with the exception, where both `errorCode` and `errorMessage` already live:

```java
// RinkoException gains:
public ProblemDetail toProblemDetail() {
    return ProblemDetail.builder(getTitle(), httpStatus.value())
        .type("/errors/" + errorCode.toLowerCase().replace('_', '-'))
        .detail(errorMessage)
        .build();
}

protected String getTitle() {
    return httpStatus.name(); // e.g., "Bad Request", "Not Found"
}
```

Each subclass overrides `getTitle()` to return a human-readable summary matching the HTTP status reason phrase.

**Rationale**: Single source of truth. The exception already holds all the data; having it produce its own ProblemDetail avoids duplicating the mapping in exception handlers. The converters in GlobalExceptionHandler become thin: `ex.toProblemDetail()`.

**Alternative considered**: Static factory on ProblemDetail: `ProblemDetail.from(RinkoException ex)`. Rejected because it creates a bidirectional dependency (dto package depending on exception package). The exception→dto direction is cleaner.

### Decision 3: Remove ApiResponse.error(), keep ApiResponse.success()

The `error()` static factory is removed from `ApiResponse`. The `success()` variants remain untouched. Controllers continue to return `ApiResponse.success(data)` for 2xx responses.

Search for all `ApiResponse.error(` call sites and replace them:
- **6 controllers** in `rinko-auth`: Replace with `throw ValidationException(...)` or `throw NotFoundException(...)`
- **1 controller** in `rinko-notify`: Replace with `throw NotFoundException(...)`

Each replacement must also set the appropriate HTTP status on the response (currently done manually before `ApiResponse.error()`). Since we're switching to throwing exceptions, the global handler will set the status automatically — **remove the manual status-setting calls**.

### Decision 4: Validation error handling (new `@Valid` support)

Add `MethodArgumentNotValidException` handling to both exception handlers. Spring's `@Valid` validation failures currently fall through to the generic `Exception` handler, returning a generic 500. This adds proper 400 handling:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletResponse response) {
    String detail = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .collect(Collectors.joining(", "));
    response.setStatus(400);
    response.setContentType("application/problem+json");
    return ProblemDetail.builder("Bad Request", 400)
        .type("/errors/validation-error")
        .detail(detail)
        .build();
}
```

### Decision 5: Content-Type header

Both exception handlers explicitly set `Content-Type: application/problem+json` on the response. The `@RestControllerAdvice` return value goes through Jackson serialization, so the ProblemDetail object is serialized to JSON automatically. The Content-Type is set explicitly to ensure RFC 7807 compliance.

For the gateway JWT filter (which writes raw bytes to the response), serialize via Jackson's `ObjectMapper` instead of string concatenation.

### Decision 6: Gateway JWT filter — use ProblemDetail builder

The filter's `unauthorized()` method currently builds a hardcoded JSON string. Replace with ProblemDetail builder, serialize with ObjectMapper:

```kotlin
private fun unauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
    exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON
    val pd = ProblemDetail.builder("Unauthorized", 401)
        .type("/errors/unauthorized")
        .detail(message)
        .build()
    val json = objectMapper.writeValueAsString(pd)
    val buffer = exchange.response.bufferFactory().wrap(json.toByteArray(StandardCharsets.UTF_8))
    return exchange.response.writeWith(Mono.just(buffer))
}
```

## Risks / Trade-offs

- **BREAKING: Client-side error parsing** — All API clients must update to parse `{type, title, status, detail, timestamp}` instead of `{code, message, data}`. Mitigation: This is an internal project; coordinate with frontend team. The change is a one-time cutover during a deployment window.
- **Gateway module dependency on rinko-infra** — The gateway currently doesn't depend on `rinko-infra`'s DTOs. Need to verify/add the dependency. Mitigation: `rinko-gateway` already shares the root POM; adding a Maven dependency on `rinko-infra` is trivial.
- **Serialization compatibility** — ProblemDetail uses `Instant` (not `LocalDateTime` like ApiResponse). This is intentional (RFC 7807 recommends ISO 8601 with timezone). Jackson serializes `Instant` to ISO 8601 by default. No custom serializer needed.
