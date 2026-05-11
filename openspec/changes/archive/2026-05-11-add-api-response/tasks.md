## 1. ApiResponse Class

- [x] 1.1 Create `ApiResponse.java` — generic wrapper with `code`, `message`, `data`, `timestamp` + static factory `success(T)` / `error(int, String)`

## 2. Global Exception Handlers

- [x] 2.1 Create `GlobalExceptionHandler.java` — Servlet `@RestControllerAdvice`, `@ConditionalOnWebApplication(SERVLET)`, maps `RinkoException` subclasses to `ApiResponse`
- [x] 2.2 Create `ReactiveGlobalExceptionHandler.kt` — Kotlin WebFlux `@RestControllerAdvice`, `@ConditionalOnWebApplication(REACTIVE)`, returns `Mono<ResponseEntity<ApiResponse<?>>>`

## 3. Auto-Configuration Registration

- [x] 3.1 Register both handlers in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 4. Build Verification

- [x] 4.1 Add `kotlin-stdlib` dependency to `rinko-infra/pom.xml` (for Kotlin handler compilation)
- [x] 4.2 Run `mvn clean compile` — verify entire project compiles
- [x] 4.3 Run `mvn clean test` — verify all tests pass

## 5. Spec Sync

- [x] 5.1 Sync delta spec to `openspec/specs/coding-standards/`
