## 1. 依赖与配置

- [x] 1.1 `rinko-auth/pom.xml` 添加 `spring-boot-starter-amqp`
- [x] 1.2 `rinko-auth-dev.yml` 添加 RabbitMQ 配置

## 2. DTO

- [x] 2.1 `RegisterRequest` 新增 `code` 字段
- [x] 2.2 新增 `SendCodeRequest`、`SendCodeResponse`

## 3. VerificationCodeService

- [x] 3.1 `sendCode()`: 生成 6 位数字 → Redis（key=`auth:verify-code:{email}`, TTL=5min）→ RabbitMQ → notify
- [x] 3.2 `verifyCode()`: 查 Redis 比对，匹配则删除并返回 true

## 4. AuthService + AuthController

- [x] 4.1 `register()` 增加验证码校验
- [x] 4.2 `POST /api/v1/auth/send-code` 端点
- [x] 4.3 `SecurityConfig` 将 `/send-code` 加入白名单

## 5. 构建

- [x] 5.1 `mvn compile -pl rinko-auth -am` 成功
