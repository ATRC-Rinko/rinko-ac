## 1. Core: RinkoException ProblemDetail mapping

- [x] 1.1 Add `toProblemDetail()` method to `RinkoException` base class that maps errorCode→type URI, errorMessage→detail, httpStatus→status/title
- [x] 1.2 Override `getTitle()` in each subclass (`ValidationException`, `UnauthorizedException`, `ForbiddenException`, `NotFoundException`, `InternalException`) to return the HTTP status reason phrase

## 2. Core: Global exception handlers return ProblemDetail

- [x] 2.1 Rewrite `GlobalExceptionHandler.java` (Servlet) to return `ProblemDetail` instead of `ApiResponse.error()`, set Content-Type to `application/problem+json`
- [x] 2.2 Rewrite `ReactiveGlobalExceptionHandler.kt` (WebFlux) to return `Mono.just(ProblemDetail)` instead of `Mono.just(ApiResponse.error())`, set Content-Type to `application/problem+json`
- [x] 2.3 Add `MethodArgumentNotValidException` handler to `GlobalExceptionHandler` (Servlet @Valid failures → 400 ProblemDetail)
- [x] 2.4 Add `WebExchangeBindException` handler to `ReactiveGlobalExceptionHandler` (WebFlux @Valid failures → 400 ProblemDetail)

## 3. Core: ApiResponse cleanup

- [x] 3.1 Remove `ApiResponse.error()` static factory method — keep only `success()` variants
- [x] 3.2 Verify all remaining `ApiResponse.success()` call sites still compile and pass tests

## 4. Gateway: JWT filter use ProblemDetail builder

- [x] 4.1 Replace inline JSON construction in `JwtAuthFilter.kt` `unauthorized()` method with `ProblemDetail.builder()` + `ObjectMapper.writeValueAsString()`

## 5. Controllers: Replace inline ApiResponse.error() with exceptions

- [x] 5.1 `AuthController.kt` — replace 2 `ApiResponse.error(400, ...)` calls with `ValidationException` throws, remove manual `exchange.response.statusCode` setting
- [x] 5.2 `OAuth2Controller.kt` — replace 2 `ApiResponse.error(400, ...)` calls with `ValidationException` throws, remove manual `exchange.response.statusCode` setting
- [x] 5.3 `PermissionCheckController.kt` — replace 2 `ApiResponse.error(400, ...)` calls with `ValidationException` throws, remove manual status setting
- [x] 5.4 `RoleController.kt` — replace 4 `ApiResponse.error(400, ...)` calls with `ValidationException` throws, remove manual status setting
- [x] 5.5 `PermissionController.kt` — replace 2 `ApiResponse.error(400, ...)` calls with `ValidationException` throws, remove manual status setting
- [x] 5.6 `TemplateController.java` — replace `ApiResponse.error(404, ...)` with `throw new NotFoundException(...)`, remove manual `response.setStatus()`

## 6. Verification

- [x] 6.1 Build all modules: `mvn clean compile -pl rinko-infra,rinko-gateway,rinko-auth,rinko-notify -am`
- [x] 6.2 Search codebase to confirm zero remaining `ApiResponse.error(` calls in non-doc source files
- [x] 6.3 Update `coding-standards` spec: replace `ApiResponse.error()` references with ProblemDetail pattern in the "Unified API Response Format" requirement
