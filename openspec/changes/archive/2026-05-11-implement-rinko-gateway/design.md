## Context

`rinko-gateway` 是 Rinko 微服务的统一入口网关，基于 Spring Cloud Gateway + WebFlux + Netty（Kotlin）。所有外部请求经网关路由到对应微服务，网关负责 JWT 鉴权、Sentinel 限流熔断和 CORS 处理。

当前 Nacos 配置中已有 5 条路由，但路径模式与实际 API 版本化路径不匹配（`/api/auth/**` vs `/api/v1/auth/**`）。

## Goals / Non-Goals

**Goals:**
- 实现 5 条路由规则，路径匹配 `/api/v1/{service}/**`
- 实现 JWT 本地验证 GlobalFilter（不依赖 rinko-auth 远程调用）
- 配置白名单路由（register、login、token/refresh、OAuth2、API docs）
- 实现 Sentinel 限流（每路由 QPS 限制）和熔断降级
- 全局异常处理（401 未授权 → RFC 7807、429 限流 → 标准响应）

**Non-Goals:**
- 不实现灰度路由（金丝雀发布）
- 不实现请求/响应日志全量记录
- 不修改下游服务的认证逻辑

## Decisions

### 1. JWT 本地验证 vs 远程调用 rinko-auth

**决策**: 网关上本地解析 JWT（使用相同 secret key），不调用 rinko-auth。

**理由**:
- 避免循环依赖（网关认证请求自己路由到自己）
- 减少网络延迟（每个请求避免额外 HTTP 调用）
- JWT 自包含设计天然支持无状态验证
- Token 黑名单检查不在此处（auth 服务内部使用，网关只验证签名+有效期）

### 2. 白名单路径

- `/api/v1/auth/register`
- `/api/v1/auth/login`
- `/api/v1/auth/token/refresh`
- `/oauth2/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`

### 3. Sentinel 限流策略

**决策**: 使用 Sentinel Gateway Adapter，按 routeId 配置规则：
- auth 路由：QPS 100（用户认证入口）
- 其他路由：QPS 50（默认）

降级时返回 HTTP 429 + RFC 7807 body。

### 4. 路由路径修正

| 原配置 | 修正后 | 目标服务 |
|--------|--------|----------|
| `/api/auth/**` | `/api/v1/auth/**` | `lb://rinko-auth` |
| `/api/oss/**` | `/api/v1/oss/**` | `lb://rinko-oss` |
| `/api/log/**` | `/api/v1/logs/**` | `lb://rinko-log` |
| `/api/notify/**` | `/api/v1/notify/**` | `lb://rinko-notify` |
| `/api/scheduler/**` | `/api/v1/scheduler/**` | `lb://rinko-scheduler` |

## Risks / Trade-offs

- **[风险] JWT secret 需要在网关和 auth 服务间保持一致** → 通过 Nacos 共享配置管理 secret，环境变量注入 `JWT_SECRET`
- **[风险] 网关成为单点** → Sentinel 限流保护网关自身不被击溃；生产环境部署多实例 + Nginx LB
- **[取舍] 网关不做 Token 黑名单检查** → 黑名单在 auth 服务中生效（token revoke 仅影响后续与 auth 交互的操作）
