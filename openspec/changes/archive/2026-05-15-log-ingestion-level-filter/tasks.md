## 1. LogIngestionService — 注入 LogLevelConfigMapper

- [x] 1.1 添加 `LogLevelConfigMapper` 字段和构造器参数

## 2. 级别缓存

- [x] 2.1 添加 `Map<String, String> levelCache`（key=`serviceName:loggerName`，value=`logLevel`），初始化为 `ConcurrentHashMap`
- [x] 2.2 添加 `Map<String, Integer> LEVEL_ORDINAL` 静态常量
- [x] 2.3 添加 `@Scheduled(fixedRate = 30000) refreshLevelCache()` 方法：全量查 `log_level_configs` 刷新缓存

## 3. 级别过滤逻辑

- [x] 3.1 添加 `shouldKeepByLevel(LogMessage)` 方法
- [x] 3.2 ERROR/WARN 直接保留
- [x] 3.3 查缓存获取阈值，无配置则保留
- [x] 3.4 比较级别序号：低于阈值则丢弃

## 4. 接入过滤链

- [x] 4.1 `ingest()` 和 `ingestBatch()` 中在 `shouldSample` 前调用 `shouldKeepByLevel`（先级别过滤，再采样）

## 5. 构建验证

- [x] 5.1 `mvn compile -pl rinko-log -am -DskipTests` 成功
