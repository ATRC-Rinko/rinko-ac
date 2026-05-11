## 1. Maven Dependencies

- [x] 1.1 Add `spring-boot-starter-data-jdbc` to `rinko-notify/pom.xml`
- [x] 1.2 Add `spring-boot-starter-mail`, `spring-cloud-starter-bus-amqp`, `spring-boot-starter-websocket` to `rinko-notify/pom.xml`
- [x] 1.3 Add `springdoc-openapi-starter-webmvc-ui` to `rinko-notify/pom.xml`

## 2. Configuration & Infrastructure

- [x] 2.1 Create `NotifyProperties.java` — channels enable, email provider, SMS provider, SMTP/SendGrid/aliyun/tencent config
- [x] 2.2 Create Flyway `V1__create_notification_history.sql`
- [x] 2.3 Create Flyway `V2__create_notification_templates.sql`
- [x] 2.4 Update `nacos-config/rinko-notify-dev.yml` — SMTP, SendGrid, aliyun/tencent SMS config

## 3. Entity & Repository

- [x] 3.1 Create `NotificationHistory.java` + MyBatis mapper (batch insert)
- [x] 3.2 Create `NotificationTemplate.java` + MyBatis mapper

## 4. Provider Layer (Multi-Provider)

- [x] 4.1 Create `EmailProvider.java` interface + `SmtpProvider.java` + `SendGridProvider.java`
- [x] 4.2 Create `SmsProvider.java` interface + `AliyunSmsProvider.java` + `TencentSmsProvider.java`
- [x] 4.3 Create `ProviderConfig.java` — `@ConditionalOnProperty` auto-selection

## 5. Channel Senders

- [x] 5.1 Create `NotificationChannel.java` interface
- [x] 5.2 Create `InAppChannel.java` — save to DB + publish `NotificationCreatedEvent`
- [x] 5.3 Create `EmailChannel.java` — delegate to `EmailProvider`
- [x] 5.4 Create `SmsChannel.java` — delegate to `SmsProvider`

## 6. Service Layer

- [x] 6.1 Create `NotifyService.java` — `send()` (publish to RabbitMQ), `sendBatch()` (dedup + batch insert), template replace, inbox query
- [x] 6.2 Create `NotifyConsumer.java` — RabbitMQ `@RabbitListener`, dispatch to channel, update status
- [x] 6.3 Create `NotificationPushService.java` — WebSocket session pool, SSE emitter pool, listen `NotificationCreatedEvent`

## 7. WebSocket / SSE

- [x] 7.1 Create `NotifyWebSocketHandler.java` — handle `ws://host/ws/notify?userId=X`
- [x] 7.2 Create `WebSocketConfig.java` — register WebSocket endpoint
- [x] 7.3 Create `NotifySseController.java` — `GET /api/v1/notify/stream` with `SseEmitter`

## 8. Controller Layer

- [x] 8.1 Create `NotifyController.java` — `POST /send`, `POST /send-batch`
- [x] 8.2 Create `InboxController.java` — `GET /inbox`, `PUT /inbox/{id}/read`, `GET /inbox/unread-count`
- [x] 8.3 Create `TemplateController.java` — CRUD `/templates`

## 9. Application Entry Point

- [x] 9.1 Create `RinkoNotifyApplication.java` — `@SpringBootApplication` + `@EnableDiscoveryClient` + `@EnableDruid`

## 10. Verification

- [x] 10.1 Run `mvn clean compile` on `rinko-notify` — verify compilation succeeds

## 11. Spec Sync

- [x] 11.1 Sync 5 new specs to `openspec/specs/` — `notify-channel`, `notify-inbox`, `notify-template`, `notify-push`, `notify-provider`
