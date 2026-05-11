## 1. Seata Server Deployment

- [x] 1.1 Add Seata Server to `docker-compose.yml` — image `seataio/seata-server:2.2.0`, Nacos registration
- [x] 1.2 Add Seata Nacos config — `seataServer.properties` for DB store mode

## 2. Shared Nacos Configuration

- [x] 2.1 Add Seata client config to `nacos-config/application-dev.yml` — tx-service-group, registry type, config type

## 3. Module Dependencies

- [x] 3.1 Add `spring-cloud-starter-alibaba-seata` to `rinko-auth/pom.xml`
- [x] 3.2 Add `spring-cloud-starter-alibaba-seata` to `rinko-log/pom.xml`
- [x] 3.3 Add `spring-cloud-starter-alibaba-seata` to `rinko-oss/pom.xml`
- [x] 3.4 Add `spring-cloud-starter-alibaba-seata` to `rinko-notify/pom.xml`
- [x] 3.5 Add `spring-cloud-starter-alibaba-seata` to `rinko-scheduler/pom.xml`

## 4. undo_log Table (each module)

- [x] 4.1 Add Flyway migration `Vx__create_undo_log.sql` to `rinko-auth`
- [x] 4.2 Add Flyway migration `Vx__create_undo_log.sql` to `rinko-log`
- [x] 4.3 Add Flyway migration `Vx__create_undo_log.sql` to `rinko-oss`
- [x] 4.4 Add Flyway migration `Vx__create_undo_log.sql` to `rinko-notify`
- [x] 4.5 Add Flyway migration `Vx__create_undo_log.sql` to `rinko-scheduler`

## 5. Verification

- [x] 5.1 Run `mvn clean compile` — verify all modules compile with Seata dependency

## 6. Spec Sync

- [x] 6.1 Sync 2 specs to `openspec/specs/` — `distributed-transaction`, `coding-standards` delta
