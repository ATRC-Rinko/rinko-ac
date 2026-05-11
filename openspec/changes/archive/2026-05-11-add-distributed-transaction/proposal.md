## Why

微服务架构中跨服务的业务操作需要分布式事务来保证数据一致性。例如：用户注册（rinko-auth）后发送欢迎邮件（rinko-notify）——两个操作跨服务、跨数据库，任一失败都需要整体回滚。Seata 是 Alibaba 开源的分布式事务解决方案，与 Spring Cloud Alibaba 生态深度集成，支持 AT（自动补偿）、TCC、SAGA 三种模式。

## What Changes

- 添加 `spring-cloud-starter-alibaba-seata` 依赖到所有业务模块
- Nacos 共享配置添加 Seata 注册中心配置（TC Server 地址、事务分组）
- Docker Compose 添加 Seata Server 服务
- 每个数据库添加 `undo_log` 表（Flyway 迁移，Seata AT 模式必需）
- `rinko-infra` 提供 `@EnableSeata` 注解或自动配置
- 更新 `coding-standards` spec：分布式事务使用 `@GlobalTransactional` 注解

## Capabilities

### New Capabilities

- `distributed-transaction`: Seata AT 模式分布式事务 — `@GlobalTransactional` 注解、`undo_log` 表、TC Server 部署

### Modified Capabilities

- `coding-standards`: 新增分布式事务规范 — 跨服务操作必须使用 `@GlobalTransactional`

## Impact

- 新增依赖：`spring-cloud-starter-alibaba-seata`（所有业务模块）
- 新增文件：Flyway `undo_log.sql`（每个模块）、Docker Compose Seata 服务
- Nacos 配置更新：`application-dev.yml` 添加 Seata 配置
- 无业务代码变更（仅基础设施配置）
