## 1. 数据库

- [ ] 1.1 `V3__create_notification_accounts.sql` — 新建表
- [ ] 1.2 实体 `NotificationAccount.java` + Mapper `NotificationAccountMapper.java` + XML

## 2. Service

- [ ] 2.1 `NotificationAccountService.java` — 按 provider 查询 enabled 账户，解析 JSON config

## 3. Provider 改造

- [ ] 3.1 `SmtpProvider.java` — 从 DB 加载 SMTP 配置
- [ ] 3.2 `NotifyProperties.java` — 移除 credential 内部类

## 4. 配置

- [ ] 4.1 `rinko-notify-dev.yml` — 移除 credential 配置项（如有）

## 5. 构建

- [ ] 5.1 `mvn compile -pl rinko-notify -am` 成功
