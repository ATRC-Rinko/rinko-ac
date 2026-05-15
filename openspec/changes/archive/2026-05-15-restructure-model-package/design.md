## Package Structure

```
com.rinko.{module}.model
├── entity/    # 数据库实体（@TableName, @TableId）
├── dto/       # 请求体（record, @Schema）
└── vo/        # 响应体（record, @Schema）
```

## VO Design

- 使用 Java `record`，字段名与实体一致
- 添加 `@Schema` 注解描述字段
- 提供静态工厂方法 `VO from(Entity e)`

## Controller Rules

- `@RequestBody` → DTO only, never Map or Entity
- Return type → VO only, never Entity
- `GET` list endpoints → `List<XxxVO>` or `PageResponse<XxxVO>`
