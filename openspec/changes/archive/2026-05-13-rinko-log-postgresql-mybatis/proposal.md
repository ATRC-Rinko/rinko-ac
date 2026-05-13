## Why

rinko-log 当前使用 Spring Data JDBC (`CrudRepository`) 操作 PostgreSQL 的 `log_level_configs` 表。而 rinko-notify、rinko-oss、rinko-scheduler 虽然已使用 MyBatis XML Mapper，但仍依赖 `spring-boot-starter-data-jdbc` 并在实体上混用 Spring Data 注解（`@Table`/`@Id`）。统一迁移到 MyBatis-Plus + PageHelper 可以：

- 统一四模块的数据访问层技术栈
- `BaseMapper<T>` 提供内建 CRUD，减少 XML 样板代码
- `LambdaQueryWrapper<T>` 提供类型安全的编译期列名校验
- PageHelper 提供声明式分页
- 移除不必要的 `spring-boot-starter-data-jdbc` 依赖

## What Changes

### rinko-log
- 替换 `LogLevelConfigRepository` (Spring Data JDBC `CrudRepository`) 为 `LogLevelConfigMapper` (MyBatis-Plus `BaseMapper`)
- 使用 `LambdaQueryWrapper` 替代派生查询方法
- 新增 `mybatis-plus-spring-boot4-starter` 和 `pagehelper-spring-boot-starter`

### rinko-notify
- `mybatis-spring-boot-starter` 3.0.4 → `mybatis-plus-spring-boot4-starter`
- 移除 `spring-boot-starter-data-jdbc`
- 实体注解 `@Table`/`@Id` → `@TableName`/`@TableId(type = IdType.INPUT)`
- application.yml: `mybatis` → `mybatis-plus` 配置项
- XML Mapper 保持不变（MyBatis-Plus 完全兼容）

### rinko-oss
- 同 rinko-notify：starter 替换 + 注解迁移 + 配置项更新

### rinko-scheduler
- 同 rinko-notify：starter 替换 + 注解迁移 + 配置项更新
- 新增 `pagehelper-spring-boot-starter`（该模块尚未引入）

### Parent POM
- 新增 `<mybatis-plus.version>` 和 `<pagehelper.version>` 版本属性
- `<dependencyManagement>` 中引入对应的 starter

## Capabilities

### New Capabilities

None. Pure implementation change — no API or behavioral changes.

### Modified Capabilities

None. All existing spec requirements unchanged.

## Impact

### 涉及模块
| 模块 | pom.xml | 实体数 | Mapper 数 | application.yml |
|------|---------|--------|-----------|-----------------|
| rinko-log | 依赖替换 | 1 | 1（新建） | 新增 MyBatis-Plus + PageHelper |
| rinko-notify | 依赖替换 | 2 | 2 | `mybatis` → `mybatis-plus` |
| rinko-oss | 依赖替换 | 3 | 3 | `mybatis` → `mybatis-plus` |
| rinko-scheduler | 依赖替换 | 3 | 3 | `mybatis` → `mybatis-plus` + 新增 pagehelper |
