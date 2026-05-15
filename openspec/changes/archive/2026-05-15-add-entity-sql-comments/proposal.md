## Why

实体类字段和 SQL 建表脚本缺少注释，影响代码可读性和数据库文档化。统一添加中文注释到所有实体类的字段和所有 SQL 建表脚本的列。

## What Changes

- 10 个实体类：字段添加 JavaDoc `/** 注释 */`
- 18 个 SQL 文件：表和列添加 `COMMENT`（PostgreSQL 支持 `COMMENT ON COLUMN`）

## Impact

| 模块 | 实体 | SQL |
|------|------|-----|
| rinko-auth | — | 4+1 undo_log |
| rinko-log | 2 | 1+1 undo_log |
| rinko-notify | 2 | 2+1 undo_log |
| rinko-oss | 3 | 3+1 undo_log |
| rinko-scheduler | 3 | 3+1 undo_log |
