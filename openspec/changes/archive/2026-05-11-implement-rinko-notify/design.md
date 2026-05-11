## Context

`rinko-notify` 是 Rinko 通知中心：3 渠道 + 实时推送 + 批量优化 + 多供应商。

## Goals / Non-Goals

**Goals:**
- 站内信、邮箱、短信 3 渠道
- WebSocket/SSE 实时推送
- 批量发送优化（去重、批量插入）
- 邮件/SMS 多供应商切换
- RabbitMQ 异步投递
- 模板变量替换

**Non-Goals:**
- 不实现消息已读回执推送到 WebSocket（仅拉模式）

## Decisions

### 1. WebSocket/SSE 双协议

同时提供 WebSocket (`ws://host/ws/notify?userId=X`) 和 SSE (`GET /api/v1/notify/stream?userId=X`)。Spring 的 `SseEmitter` 用于 SSE，`WebSocketHandler` 用于 WebSocket。站内信写入后发布 `NotificationCreatedEvent`，PushService 推送到对应 userId 的连接。

### 2. 批量发送

`POST /api/v1/notify/send-batch` 接受 `List<String> recipients`。去重按 `(channel, templateCode, recipient)`。MyBatis batch insert 写入。

### 3. 多供应商切换

策略模式 + 配置选择：
- 邮件：`EmailProvider` → `SmtpProvider` / `SendGridProvider`, key: `rinko.notify.email.provider`
- 短信：`SmsProvider` → `AliyunSmsProvider` / `TencentSmsProvider`, key: `rinko.notify.sms.provider`
- 默认：email=smtp, sms=aliyun

### 4. 推送事件模型

`NotificationCreatedEvent` → `NotificationPushService` 监听 → 查找 WebSocket/SSE 连接 → 推送。

## Risks / Trade-offs

- **[风险] WebSocket 连接数过多** → 单节点 10000 连接限制，Nginx LB 横向扩展
- **[取舍] SSE 不支持 IE** → 降级为轮询 API
