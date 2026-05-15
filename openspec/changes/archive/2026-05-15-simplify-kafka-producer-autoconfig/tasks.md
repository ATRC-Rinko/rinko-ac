## 1. 删除自定义 KafkaProducerConfig

- [x] 1.1 删除 `KafkaProducerConfig.java`

## 2. 简化 KafkaLogAppenderHolder

- [x] 2.1 移除 `@AutoConfigureAfter`、`@PostConstruct`、`@Getter`、`@RequiredArgsConstructor`
- [x] 2.2 构造器直接赋值静态字段

## 3. 迁移 producer 参数到 yml

- [x] 3.1 `nacos-config/application-dev.yml` 新增 `spring.kafka.producer` 配置（acks/retries/超时参数）

## 4. 构建验证

- [x] 4.1 `mvn compile -pl rinko-infra -am -DskipTests` 成功
