## Why

`rinko-scheduler` 是分布式定时任务调度服务，负责 Cron 调度、DAG 工作流编排、故障转移与告警。当前模块完全是骨架状态，基础设施（PostgreSQL、RabbitMQ、Redis）已在 `docker-compose.yml` 中配置。多实例部署时需确保每个任务只执行一次（分布式锁），并通过 DAG 依赖实现复杂工作流。

## What Changes

- 添加 Maven 依赖：`spring-boot-starter-data-jdbc`、`spring-boot-starter-quartz`（Quartz JobStore JDBC）
- **分布式调度**：Quartz JDBC JobStore — 多个实例共享 PostgreSQL 任务状态，自动选主执行
- **任务类型**：HTTP 回调、Shell 命令、Spring Bean 方法
- **Cron 表达式**：标准 Quartz Cron + 简单间隔触发
- **DAG 编排**：`task_dependencies` 表，前置任务完成后自动触发后续
- **失败处理**：重试次数 + 指数退避 + 失败告警
- **REST API**：`POST/GET/PUT/DELETE /api/v1/scheduler/jobs` + `POST /trigger` + `POST /pause` + `GET /executions`
- Flyway 迁移：`scheduler_jobs` + `scheduler_executions` + `task_dependencies` + Quartz 表

## Capabilities

### New Capabilities

- `scheduler-job`: 任务管理 — CRUD、Cron、HTTP/Shell/Bean 三种类型
- `scheduler-dag`: DAG 工作流 — 任务依赖、自动触发后续
- `scheduler-execution`: 执行追踪 — 历史记录、重试、故障告警

### Modified Capabilities

<!-- 无需修改 -->

## Impact

- 新增依赖：`spring-boot-starter-data-jdbc`、`spring-boot-starter-quartz`
- 新增文件：~15 个 Java 文件
- Flyway 迁移：业务表 3 个 + Quartz 内置表 11 个
