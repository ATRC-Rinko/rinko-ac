## Context

项目有 6 个业务模块各有独立 PostgreSQL 数据库。跨服务操作需要 Seata 分布式事务。Seata AT 模式通过 `undo_log` 表实现自动回滚，对业务代码侵入最小。

## Goals / Non-Goals

**Goals:**
- Seata AT 模式——业务代码只需 `@GlobalTransactional` 注解
- 所有模块数据库添加 `undo_log` 表
- Docker Compose 部署 Seata Server
- Nacos 统一配置 Seata 连接

**Non-Goals:**
- 不实现 TCC 或 SAGA 模式
- 不修改已有业务代码（预留 `@GlobalTransactional` 注解位置）

## Decisions

### 1. AT 模式（默认）

**决策**: 使用 Seata AT（Auto-Compensation）模式。

**理由**: AT 模式对业务代码侵入最小——只需 `@GlobalTransactional` 注解。Seata 自动记录 undo_log，失败时回滚。适用于本项目的大部分场景（单服务内多表操作跨服务）。

### 2. Seata Server 部署

**决策**: Docker Compose 添加 `seata-server` 服务，注册到 Nacos。

配置：
```yaml
seata:
  registry:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:18848
      namespace: rinko
      group: anchorage
  config:
    type: nacos
  store:
    mode: db
    db:
      datasource: druid
      db-type: postgresql
```

### 3. undo_log 表

每个模块的 PostgreSQL 数据库都需要 Flyway 迁移创建：
```sql
CREATE TABLE IF NOT EXISTS undo_log (
    id SERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info BYTEA NOT NULL,
    log_status INT NOT NULL,
    log_created TIMESTAMP NOT NULL,
    log_modified TIMESTAMP NOT NULL,
    UNIQUE (xid, branch_id)
);
```

### 4. Seata 事务分组

事务分组 `rinko-tx-group`，映射到 Seata Server 集群 `default`。所有模块共享同一分组。

## Risks / Trade-offs

- **[风险] undo_log 增加存储开销** → 每个分支事务一条 undo_log 记录，定期清理（Seata 默认 7 天）
- **[风险] AT 模式不支持所有 SQL** → 仅支持 INSERT/UPDATE/DELETE，不支持 DDL
- **[取舍] 不引入 TCC 模式** → 当前业务场景 AT 足够，TCC 需要业务代码配合实现 try/confirm/cancel
