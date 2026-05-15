## Context

`log_level_configs` 表存储了按 `(serviceName, loggerName)` 粒度的日志级别覆盖规则。`LogIngestionService` 摄入每条日志时已知 `service`（来源服务）和 `className`（logger 名），应据此查询配置并过滤。

## Goals / Non-Goals

**Goals:**
- 摄入前按 `(service, className)` 查询配置的级别阈值
- 低于阈值的日志丢弃
- ERROR/WARN 始终保留（不受级别过滤影响）
- 配置缓存避免每次查 DB

**Non-Goals:**
- 不支持 loggerName 通配符（精确匹配）
- 不修改 `shouldSample` 的采样逻辑（采样独立于级别过滤）

## Decisions

1. **两级过滤链**: 级别过滤在前，采样在后。ERROR/WARN 两级过滤器都豁免。
2. **精确匹配**: `LogMessage.className()` 直接匹配 `LogLevelConfig.loggerName`，不做前缀/通配。
3. **缓存策略**: `Map<String, String>`，key=`serviceName:loggerName`，value=`level`。`@Scheduled(fixedRate = 30000)` 全量刷新。
4. **级别比较**: `Map.of("TRACE",0,"DEBUG",1,"INFO",2,"WARN",3,"ERROR",4)` 做序号比较。

## Risks / Trade-offs

- **Risk: 缓存延迟** — 管理员修改级别后最多 30s 才生效。**Mitigation: 30s 对于日志过滤场景可接受。后期可通过 Spring Cloud Bus 推送失效。**

- **Risk: 单条全量查询** — `getAllConfigs()` 每次全量查 `log_level_configs` 表。表数据量极小（手工配置），无需分页。
