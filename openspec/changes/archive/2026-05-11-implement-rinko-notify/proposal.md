## Why

`rinko-notify` 是 Rinko 的通知服务，负责多渠道消息推送（站内信、邮箱、短信）、实时推送（WebSocket/SSE）、批量发送优化和多供应商切换。当前模块完全是骨架状态，基础设施（RabbitMQ、PostgreSQL、Redis）已在 `docker-compose.yml` 中配置。

## What Changes

- **3 个通知渠道**：站内信（PostgreSQL）、邮箱（Spring Mail + SMTP）、短信（阿里云 SDK）
- **实时推送**：WebSocket/SSE 双协议支持，客户端可选择连接方式订阅用户消息
- **批量发送优化**：`POST /api/v1/notify/send-batch` — 单次请求发送多个收件人，内部去重 + 批量插入
- **供应商切换**：邮件支持 SMTP / SendGrid 双供应商，短信支持阿里云 / 腾讯云双供应商，通过配置 `provider` 切换
- **模板管理**：`{var}` 变量替换，CRUD API
- **异步投递**：RabbitMQ 解耦，`POST /send` → Queue → Consumer → Channel
- Flyway 迁移

## Capabilities

### New Capabilities

- `notify-channel`: 多渠道通知 — 站内信/邮箱/短信，异步 RabbitMQ，批量发送
- `notify-inbox`: 站内信管理 — 消息列表、已读/未读
- `notify-template`: 模板管理 — CRUD、变量替换
- `notify-push`: 实时推送 — WebSocket/SSE，用户订阅，消息即时到达
- `notify-provider`: 供应商切换 — SMTP/SendGrid（邮件）、阿里云/腾讯云（短信）

### Modified Capabilities

<!-- 无需修改 -->

## Impact

- 新增依赖：`spring-boot-starter-mail`、`spring-boot-starter-data-jdbc`、`spring-boot-starter-websocket`
- 新增文件：~22 个 Java 文件
- Flyway：`notification_history`、`notification_templates`
