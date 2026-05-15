## 种子数据设计

| 分组 | 配置数 | 级别 | 说明 |
|------|--------|------|------|
| 各服务核心包 | 6 | INFO | 业务日志默认 INFO |
| rinko-gateway filter | 1 | DEBUG | 认证过滤链保持 DEBUG |
| rinko-log consumer | 1 | DEBUG | Kafka 消费者连接问题排查 |
| rinko-oss media | 1 | WARN | ffmpeg 转码日志降噪 |
| org.springframework | 6 | WARN | Spring 框架日志全局降噪 |
| org.apache.kafka | 6 | WARN | Kafka 客户端日志全局降噪（除 rinko-log） |

使用 `ON CONFLICT DO NOTHING` 确保幂等，重复执行不报错。
