## 1. Maven Dependencies

- [x] 1.1 Add `spring-cloud-starter-gateway` to `rinko-gateway/pom.xml`
- [x] 1.2 Add `spring-cloud-starter-alibaba-sentinel` and `sentinel-spring-cloud-gateway-adapter` to `rinko-gateway/pom.xml`
- [x] 1.3 Add `jjwt-api` dependency to `rinko-gateway/pom.xml` (for JWT parsing)

## 2. Application Entry Point

- [x] 2.1 Create `RinkoGatewayApplication.kt` — `@SpringBootApplication` + `@EnableDiscoveryClient`
- [x] 2.2 Update `rinko-gateway/src/main/resources/application.yml` — add `optional:nacos:rinko-gateway-dev.yml` and SpringDoc config

## 3. JWT Authentication Filter

- [x] 3.1 Create `JwtAuthFilter.kt` — `GlobalFilter` that validates Bearer token, skips whitelist paths, injects `X-User-Id`/`X-User-Roles` headers
- [x] 3.2 Create `GatewayAuthProperties.kt` — `@ConfigurationProperties(prefix = "rinko.gateway.auth")` with `jwtSecret` and `whitelistPaths`

## 4. Sentinel & Resilience

- [x] 4.1 Create `SentinelGatewayConfig.kt` — Sentinel gateway adapter, rate limiting rules, circuit breaker
- [x] 4.2 Create `GatewayExceptionHandler.kt` — global error handling: 401 → RFC 7807, 429 → "Too Many Requests", 503 → "Service Unavailable"

## 5. CORS Configuration

- [x] 5.1 Create `GatewayCorsConfig.kt` — global CORS configuration using `rinko.cors` properties from rinko-infra

## 6. Nacos Route Configuration

- [x] 6.1 Update `nacos-config/rinko-gateway-dev.yml` — fix route paths to `/api/v1/**`, add JWT secret and Sentinel rules

## 7. Verification

- [x] 7.1 Run `mvn clean compile` on `rinko-gateway` — verify compilation succeeds
- [x] 7.2 Run `mvn clean test` on `rinko-gateway` — verify tests pass

## 8. Spec Sync

- [x] 8.1 Sync 3 new specs to `openspec/specs/` — `gateway-routing`, `gateway-auth`, `gateway-resilience`
