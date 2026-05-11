## Why

`rinko-gateway` 是整个微服务架构的统一入口，负责请求路由、JWT 鉴权、Sentinel 限流熔断和 CORS 跨域处理。当前该模块完全是骨架状态（仅 pom.xml + application.yml），Nacos 中已有 5 条路由定义但路径与实际 API 不匹配（`/api/auth/**` vs `/api/v1/auth/**`）。需要实现网关核心功能，使所有微服务通过网关统一暴露。

## What Changes

- 添加 Maven 依赖：`spring-cloud-starter-gateway`、`spring-cloud-starter-alibaba-sentinel`、`sentinel-spring-cloud-gateway-adapter`、JWT 解析（jjwt-api）
- 实现 `JwtAuthFilter`（GlobalFilter）：从 Authorization header 提取 Bearer Token，本地验证 JWT 签名和有效期，提取 userId/roles 注入请求头
- 配置白名单路径：`/api/v1/auth/register`、`/api/v1/auth/login`、`/api/v1/auth/token/refresh`、`/oauth2/**`、`/v3/api-docs/**`、`/swagger-ui/**`
- 实现 Sentinel 网关限流：按路由配置 QPS 限流和熔断降级
- 配置 CORS：复用 `rinko-infra` 的 `CorsProperties`
- 实现全局异常处理：认证失败返回 401（RFC 7807），限流返回 429
- 更新 Nacos 路由配置：修正路径匹配模式为 `/api/v1/auth/**` 等
- 创建 `RinkoGatewayApplication.kt`：`@SpringBootApplication` + `@EnableDiscoveryClient`

## Capabilities

### New Capabilities

- `gateway-routing`: 路由配置与转发 — 5 条路由规则、路径重写、健康检查
- `gateway-auth`: JWT 鉴权 — Bearer Token 本地验证、白名单、请求头注入
- `gateway-resilience`: Sentinel 限流熔断 — QPS 限流、服务降级、429 响应

### Modified Capabilities

<!-- 无需修改已有 capability -->

## Impact

- 影响文件：
  - `rinko-gateway/pom.xml` — 添加 Gateway + Sentinel + JWT 依赖
  - `rinko-gateway/src/main/kotlin/com/rinko/gateway/` — 全新实现（Kotlin + WebFlux）
  - `nacos-config/rinko-gateway-dev.yml` — 修正路由路径、添加 Sentinel 配置
  - `rinko-gateway/src/main/resources/application.yml` — 添加 Nacos 配置导入
- 依赖新增：`spring-cloud-starter-gateway`、`spring-cloud-starter-alibaba-sentinel`、`sentinel-spring-cloud-gateway-adapter`
- 无下游 API 变更
