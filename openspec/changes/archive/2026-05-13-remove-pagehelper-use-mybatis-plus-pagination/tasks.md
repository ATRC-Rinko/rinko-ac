## 1. 父 POM — 移除 PageHelper 版本管理

- [x] 1.1 移除 `<pagehelper.version>` 属性
- [x] 1.2 移除 `<dependencyManagement>` 中的 `pagehelper-spring-boot-starter` 条目

## 2. 模块 POM — 移除 PageHelper 依赖

- [x] 2.1 移除 `rinko-log/pom.xml` 中的 pagehelper 依赖
- [x] 2.2 移除 `rinko-notify/pom.xml` 中的 pagehelper 依赖
- [x] 2.3 移除 `rinko-oss/pom.xml` 中的 pagehelper 依赖
- [x] 2.4 移除 `rinko-scheduler/pom.xml` 中的 pagehelper 依赖

## 3. 配置 — 移除 pagehelper 配置段

- [x] 3.1 移除 `rinko-log/application.yml` 中的 pagehelper 配置
- [x] 3.2 移除 `rinko-scheduler/application.yml` 中的 pagehelper 配置
- [x] 3.3 移除 `nacos-config/rinko-log-dev.yml` 中的 pagehelper 配置

## 4. rinko-oss — 添加 MyBatis-Plus 分页拦截器

- [x] 4.1 新建 `MybatisPlusConfig.java` 注册 `PaginationInnerInterceptor` Bean

## 5. rinko-oss — 替换 FileService 分页实现

- [x] 5.1 `listFiles()`: `PageHelper.startPage()` + `selectList()` + `PageInfo` → `Page<T>` + `selectPage()`
- [x] 5.2 移除 `PageHelper`、`PageInfo` import，添加 `Page` import

## 6. 构建验证

- [x] 6.1 `mvn compile -pl rinko-oss -am -DskipTests` 成功
