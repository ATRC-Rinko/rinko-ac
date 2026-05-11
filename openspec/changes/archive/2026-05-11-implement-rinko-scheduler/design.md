## Context

`rinko-scheduler` 是分布式任务调度服务。Quartz + JDBC JobStore 实现多实例下任务唯一执行。DAG 依赖存储在 PostgreSQL。

## Goals / Non-Goals

**Goals:**
- Quartz JDBC JobStore 分布式调度
- 3 种任务类型：HTTP / Shell / Bean
- Cron + 简单间隔触发器
- DAG 依赖编排
- 执行历史 + 重试 + 故障告警
- REST API 管理

**Non-Goals:**
- 不实现动态分片（MapReduce 风格）

## Decisions

### 1. Quartz JDBC JobStore 实现分布式

**决策**: 使用 `spring-boot-starter-quartz` + JDBC JobStore。Quartz 内置 PostgreSQL 表（11 张），通过数据库行锁保证单实例执行。

### 2. 任务类型策略模式

```java
interface JobExecutor { void execute(JobContext ctx); }
```
- `HttpJobExecutor` — RestTemplate 调用指定 URL
- `ShellJobExecutor` — ProcessBuilder 执行命令
- `BeanJobExecutor` — 反射调用 Spring Bean 方法

### 3. DAG 依赖

`scheduler_dependencies` 表：`(job_id, depends_on_job_id)`。任务执行成功后检查是否有下游任务需要触发。

### 4. 重试与退避

最多重试 3 次，指数退避：1s → 4s → 9s。全部失败后标记 FAILED，可选发送告警通知。

## Risks / Trade-offs

- **[风险] Quartz 表未就绪时启动失败** → Flyway 迁移包含 Quartz DDL，先于 Quartz 初始化
- **[取舍] 不支持秒级精度** → Quartz 最小精度为秒，满足业务需求
