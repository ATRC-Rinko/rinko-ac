## Context

当前项目同时依赖 PageHelper 和 MyBatis-Plus。实际只有 `FileService.listFiles()` 使用分页。PageHelper 通过 ThreadLocal 传递分页参数（`PageHelper.startPage(page, size)`），下一个 SQL 自动包装 COUNT + LIMIT。MyBatis-Plus 的分页通过 `PaginationInnerInterceptor` 拦截器 + `Page<T>` 参数显式传递，不依赖 ThreadLocal。

## Goals / Non-Goals

**Goals:**
- 移除 PageHelper 依赖（父 POM + 4 模块 POM + 2 本地 yml + 1 Nacos yml）
- 替换 `FileService.listFiles()` 为 MyBatis-Plus `selectPage()`
- 注册 `PaginationInnerInterceptor` 让分页生效

**Non-Goals:**
- 不新增分页端点
- 不修改 `PageResponse` 类本身（API 契约不变）

## Decisions

1. **`PaginationInnerInterceptor` 放在 rinko-oss 模块**: 当前仅 oss 模块有分页需求。其他模块移除 pagehelper 后如需分页，自行添加 interceptor 配置。

2. **`selectPage(new Page<>(page, size), wrapper)`**: MyBatis-Plus 的 `BaseMapper.selectPage()` 自动添加 COUNT 查询和 LIMIT 子句，返回 `Page<T>` 包含 `records`、`total`、`pages`、`current`、`size`。

3. **配置类放在 `config` 包**: `com.rinko.oss.config.MybatisPlusConfig` 与已有的 `OssProperties` 平级。

4. **移除 Nacos 中的 pagehelper 配置**: `nacos-config/rinko-log-dev.yml` 中残留的 pagehelper 段一并删除。

## Risks / Trade-offs

- **Risk: rinko-log 的 Nacos 配置删除后，其他环境未同步** — `nacos-config/rinko-log-dev.yml` 是本地的 Nacos 模拟配置，生产环境的 Nacos 配置需单独运维。**Mitigation: 本次变更仅修改本地文件；生产 Nacos 配置在部署时同步清理。**

- **Risk: `Page<T>` 与 `PageInfo` 的 `total` 类型差异** — `PageInfo.getTotal()` 返回 `long`，`Page.getTotal()` 也返回 `long`，`PageResponse` 构造参数接受 `long`，无兼容性问题。
