## Context

四个模块目前使用不同的数据库访问方式：

- **rinko-log**: Spring Data JDBC `CrudRepository` → PostgreSQL `log_level_configs`
- **rinko-notify**: MyBatis XML Mapper + Spring Data JDBC 注解混用 → PostgreSQL
- **rinko-oss**: MyBatis XML Mapper + Spring Data JDBC 注解混用 → PostgreSQL
- **rinko-scheduler**: MyBatis XML Mapper + Spring Data JDBC 注解混用 → PostgreSQL

父项目 Spring Boot 4.0.0，已使用 `druid-spring-boot-4-starter` 连接池。

统一迁移到 MyBatis-Plus + PageHelper：rinko-log 需要新建 Mapper 接口并替换 `CrudRepository`；其他三个模块只需做 starter 替换和注解迁移，保留现有 XML Mapper。

## Goals / Non-Goals

**Goals:**
- 四模块统一使用 `mybatis-plus-spring-boot4-starter` + `pagehelper-spring-boot-starter`
- 移除所有 `spring-boot-starter-data-jdbc` 依赖
- 实体注解统一为 MyBatis-Plus `@TableName` / `@TableId(type = IdType.INPUT)`
- 保留所有现有 XML Mapper 文件（MyBatis-Plus 完全兼容）
- 所有 API 契约和行为不变

**Non-Goals:**
- 不改动现有 XML Mapper SQL
- 不将 XML Mapper 迁移为注解/BaseMapper（后续迭代可做）
- 不修改 Flyway 迁移脚本
- 不修改除 application.yml、pom.xml、实体、Mapper 接口外的业务代码

## Decisions

1. **XML Mapper 保留**: rinko-notify/oss/scheduler 的现有 MyBatis XML 文件保持不变。MyBatis-Plus 100% 兼容 MyBatis 原生 XML Mapper，无需修改。

2. **Mapper 接口不强制继承 `BaseMapper`**: 现有的 `@Mapper` 接口保持纯 MyBatis 风格（不添加 `extends BaseMapper<T>`），因为所有方法已通过 XML 映射。只有 rinko-log 的新 Mapper 使用 `BaseMapper`。

3. **`@TableId(type = IdType.INPUT)`**: 所有四模块使用 `SnowflakeIdGenerator` 手动生成 ID，因此使用 `INPUT` 策略而非数据库自增。

4. **配置项 `mybatis` → `mybatis-plus`**: MyBatis-Plus 识别 `mybatis-plus` 前缀配置。原有 `mybatis.mapper-locations` 等配置需迁移到对应 `mybatis-plus` 键下，或直接删除（MyBatis-Plus 默认自动扫描 `classpath*:mapper/**/*.xml`）。

5. **PageHelper 配置保留**: rinko-notify 和 rinko-oss 已有 PageHelper 配置（在 Nacos 或本地 yml 中）。rinko-scheduler 新增 PageHelper starter，配置页面对话方言 `postgresql`。

6. **rinko-scheduler 不需要 `spring-boot-starter-data-jdbc`**: Quartz JDBC 作业存储直接使用 `javax.sql.DataSource`，不依赖 Spring Data JDBC。移除该依赖不影响 Quartz。

## Risks / Trade-offs

- **Risk: MyBatis-Plus 自动配置可能冲突** — 四个模块同时有 Druid (`druid-spring-boot-4-starter`)、MyBatis-Plus、PageHelper 三个自动配置。**Mitigation**: Druid 只创建 `DataSource` bean；MyBatis-Plus 创建 `SqlSessionFactory` 使用该 `DataSource`；PageHelper 注册拦截器 — 三者职责不重叠，已知兼容。

- **Risk: `@Table` 移除可能影响某些 Spring Data 工具** — 若存在直接反射读取 `@Table` 注解的工具代码会受影响。**Mitigation**: 项目中对 `@Table` 的依赖仅限于 Spring Data JDBC 的 schema 推导，MyBatis-Plus 的 `@TableName` 功能等效。

## Open Questions

None.
