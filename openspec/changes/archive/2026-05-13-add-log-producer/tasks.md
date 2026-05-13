## 1. rinko-infra — 添加 spring-kafka 依赖

- [x] 1.1 `rinko-infra/pom.xml` 新增 `org.springframework.kafka:spring-kafka` 依赖

## 2. rinko-infra — Kafka 生产者配置

- [x] 2.1 新建 `KafkaProducerConfig.java`，创建 `KafkaTemplate<String, String>` Bean（`ProducerFactory` 使用 `StringSerializer`）

## 3. rinko-infra — KafkaLogAppender

- [x] 3.1 新建 `KafkaLogAppenderHolder.java`：Spring `@Component`，持有静态 `KafkaTemplate<String, String>`，通过 `@PostConstruct` 注入
- [x] 3.2 新建 `KafkaLogAppender.java`：扩展 `AppenderBase<ILoggingEvent>`，复用 `JsonEncoder` 格式化，从 Holder 获取 KafkaTemplate 发送到 `rinko-logs`

## 4. 业务模块 — logback-spring.xml 添加 KAFKA appender

- [x] 4.1 `rinko-auth/logback-spring.xml`：添加 KAFKA appender（使用 `KafkaLogAppender`），设置 topic 为 `rinko-logs`
- [x] 4.2 `rinko-gateway/logback-spring.xml`：同上
- [x] 4.3 `rinko-oss/logback-spring.xml`：同上（如不存在则创建）
- [x] 4.4 `rinko-scheduler/logback-spring.xml`：同上（如不存在则创建）
- [x] 4.5 `rinko-notify/logback-spring.xml`：同上（如不存在则创建）
- [x] 4.6 `rinko-log/logback-spring.xml`：确认不添加 KAFKA appender（避免循环）

## 5. Nacos 配置 — Kafka bootstrap-servers

- [x] 5.1 `rinko-auth` 的 Nacos 配置中添加 `spring.kafka.bootstrap-servers`
- [x] 5.2 `rinko-gateway` 的 Nacos 配置中添加 `spring.kafka.bootstrap-servers`
- [x] 5.3 `rinko-oss` 的 Nacos 配置中添加 `spring.kafka.bootstrap-servers`
- [x] 5.4 `rinko-scheduler` 的 Nacos 配置中添加 `spring.kafka.bootstrap-servers`

## 6. 构建验证

- [x] 6.1 `mvn compile -pl rinko-infra -am -DskipTests` 成功
- [x] 6.2 `mvn compile -pl rinko-auth,rinko-gateway,rinko-oss,rinko-scheduler,rinko-notify -am -DskipTests` 成功
