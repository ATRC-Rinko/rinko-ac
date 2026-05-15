## Why

当前 `KafkaProducerConfig` 手动创建 `ProducerFactory` 和 `KafkaTemplate` Bean，代码冗长且需硬编码 producer 参数。Spring Boot 的 `KafkaAutoConfiguration` 已提供自动配置，只需在 yml 中设置 `spring.kafka.producer.*` 即可。删除自定义配置类可简化代码、减少维护负担。

## What Changes

- 删除 `KafkaProducerConfig.java`（Spring Boot auto-config 替代）
- 简化 `KafkaLogAppenderHolder.java`（移除 `@AutoConfigureAfter`，直接注入自动配置的 `KafkaTemplate`）
- 将 producer 超时/重试参数迁移到 `application-dev.yml`（Nacos 共享配置）

## Capabilities

None.

## Impact

| 文件 | 操作 |
|------|------|
| `KafkaProducerConfig.java` | 删除 |
| `KafkaLogAppenderHolder.java` | 简化（移除 @AutoConfigureAfter、@PostConstruct） |
| `nacos-config/application-dev.yml` | 新增 `spring.kafka.producer.*` 配置 |
