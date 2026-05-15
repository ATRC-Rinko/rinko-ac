## Why

当前注册接口只需要 username/email/password，无邮箱验证，可任意注册。需增加邮箱验证码校验：注册前先发验证码到邮箱，注册时校验。

## What Changes

- `RegisterRequest` 新增 `code` 字段
- 新增 `POST /api/v1/auth/send-code` 发送验证码（生成 6 位数字 → Redis 5min TTL → RabbitMQ → notify 发送邮件）
- `AuthService.register()` 校验 Redis 中验证码，校验后删除
- rinko-auth 新增 `spring-boot-starter-amqp` 依赖

## Capabilities

None — API 扩展，不修改现有行为。
