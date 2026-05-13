## Context

rinko-oss 和 rinko-notify 已完成 MyBatis-Plus starter 迁移（`mybatis-plus-spring-boot4-starter`），但 Mapper 接口仍为纯 MyBatis 风格（不继承 `BaseMapper`），所有 CRUD 通过 XML 文件中的手写 SQL 完成。MyBatis-Plus 的 `BaseMapper<T>` 提供内建 CRUD 方法，`LambdaQueryWrapper<T>` / `LambdaUpdateWrapper<T>` 提供编译期类型安全的动态查询，可消除大量手写 SQL 样板。

两模块已配置 `mybatis-plus.configuration.map-underscore-to-camel-case: true`（oss 还有 `type-aliases-package`），实体已使用 `@TableName` / `@TableId(type = IdType.INPUT)` 注解。因此 `BaseMapper` + Wrapper 开箱即用。

## Goals / Non-Goals

**Goals:**
- 所有 Mapper 接口继承 `BaseMapper<T>`，删除与 BaseMapper 重复的方法
- SELECT 查询全部改用 `LambdaQueryWrapper` + `selectList()`/`selectOne()`/`selectCount()`
- 部分字段 UPDATE 改用 `LambdaUpdateWrapper` + `update()`
- 删除所有纯 CRUD 的 XML Mapper 文件（5 个）
- `NotificationHistoryMapper.batchInsert` 保留 XML（BaseMapper 无批量 insert）

**Non-Goals:**
- 不改动 rinko-scheduler 模块
- 不改动 `@Insert` / `@Update` / `@Delete` / `@Select` 注解方式（当前无此类用法）
- 不改动 Controller API 签名或返回格式
- 不增加或删除任何 REST 端点
- 不引入 MyBatis-Plus `IService` 层

## Decisions

1. **保留批量 insert 的 XML**: `NotificationHistoryMapper.xml` 中的 `batchInsert` 使用 `<foreach>` 一次性插入多条记录，性能优于逐条 `insert()`。MyBatis-Plus `BaseMapper` 无等价方法，故保留此 XML 片段。其他所有 XML 文件删除。

2. **LambdaUpdateWrapper 做部分字段更新**: `updateStatus` 和 `markRead` 仅更新 2-3 个字段，使用 `LambdaUpdateWrapper.set()` 比 `updateById()` 更精准（避免全字段更新和并发覆盖风险），且无需先 SELECT 再 UPDATE。

3. **移除未使用方法**: `FileMetadataMapper.findByParentId` 和 `FileVersionRepository.getMaxVersion` 在代码中无调用方，直接删除。

4. **`countByParentId` → `selectCount`**: LambdaQueryWrapper 支持 `.eq().eq()` 链式条件，替代 `SELECT count(*) FROM ... WHERE parent_id = ? AND is_directory = false`。

5. **orderBy 条件**: 原 XML 中的 `ORDER BY created_at DESC` / `ORDER BY version DESC` / `ORDER BY code` 在 Wrapper 中用 `.orderByDesc(C::getCreatedAt)` 替代。

6. **可选 `isRead` 过滤器**: `findByRecipientAndChannel` 的第三个参数 `Boolean isRead` 可为 null，使用 `.eq(isRead != null, NotificationHistory::isRead, isRead)` 条件化添加 WHERE 子句。

## Risks / Trade-offs

- **Risk: XML resultMap 中的显式列映射丢失** — 原 XML 使用 `<resultMap>` 做显式 `column → property` 映射。删除后依赖 `map-underscore-to-camel-case: true` 自动映射。**Mitigation**: 该配置已启用；若所有字段均为 `snake_case → camelCase` 转换，自动映射等效。

- **Risk: `batchInsert` 性能** — 保留 XML 的 `batchInsert` 是为确保批量发送通知时性能不变。**No change to this path.**

- **Risk: `LambdaQueryWrapper.eq(boolean condition, ...)` 的 null 安全** — `isRead` 为 null 时不添加 `is_read = ?` 条件，与原 XML `<if test="isRead != null">` 行为一致。
