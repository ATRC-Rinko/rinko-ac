## 1. docker-compose.yml

- [x] 1.1 Kafka 服务添加 `KAFKA_CREATE_TOPICS: rinko-logs:1:1` 环境变量

## 2. KafkaProducerConfig — 调大超时参数

- [x] 2.1 `request.timeout.ms`: 3000 → 10000
- [x] 2.2 新增 `delivery.timeout.ms`: 30000
- [x] 2.3 新增 `retry.backoff.ms`: 500
- [x] 2.4 `max.block.ms`: 10000 → 30000
