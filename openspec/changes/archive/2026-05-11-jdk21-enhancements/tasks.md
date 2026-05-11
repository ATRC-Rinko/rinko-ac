## 1. Build-Time JVM Configuration

- [x] 1.1 Create `.mvn/jvm.config` — `--add-opens` for java.base modules + `-XX:+EnableDynamicAgentLoading`
- [x] 1.2 Run `./mvnw clean compile` — verify no `InaccessibleObjectException` warnings

## 2. Virtual Threads Configuration

- [x] 2.1 Add `spring.threads.virtual.enabled=true` to `nacos-config/application-dev.yml`
- [x] 2.2 Run `./mvnw clean test` — verify tests pass with Virtual Threads enabled (no `synchronized` pinning issues)

## 3. Docker Runtime JVM Configuration

- [x] 3.1 Update all 6 `Dockerfile` files — add `ENV JAVA_OPTS=--add-opens ...` with JDK 21 module exports

## 4. Verify Warnings Eliminated

- [x] 4.1 Run `./mvnw clean test -pl rinko-auth` — verify no "Dynamic loading of agents" warning in test output
- [x] 4.2 Verify no `java.lang.reflect.InaccessibleObjectException` warnings in any module

## 5. Spec Sync

- [x] 5.1 Sync delta specs to main `openspec/specs/` — `coding-standards`, `configuration-standards`
