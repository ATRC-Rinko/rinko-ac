## 1. Add Lombok Dependency

- [x] 1.1 Add `<lombok.version>1.18.38</lombok.version>` to root `pom.xml` properties
- [x] 1.2 Add `org.projectlombok:lombok` to root `pom.xml` `<dependencyManagement>` with `<scope>provided</scope>`
- [x] 1.3 Add Lombok as a global `<dependency>` in root `pom.xml` with `<scope>provided</scope>` (inherited by all modules)
- [x] 1.4 Create `lombok.config` at project root with `lombok.addLombokGeneratedAnnotation = true`

## 2. Refactor DTOs to Record

- [x] 2.1 Refactor `rinko-log/dto/LogMessage.java` — class → `record`, remove manual getters/setters
- [x] 2.2 Refactor `rinko-infra/dto/PageResponse.java` — class → `record`, remove manual constructor/getters

## 3. Refactor Mutable Beans to Lombok

- [x] 3.1 Add `@Data` to `rinko-log/entity/LogEntry.java` and `LogLevelConfig.java` — remove manual getters/setters
- [x] 3.2 Add `@Getter @Setter` to `LogProperties.java`, `CorsProperties.java`, `DruidDataSourceProperties.java` — remove manual getters/setters

## 4. Verification

- [x] 4.1 Run `mvn clean compile` — verify compilation succeeds with Lombok + records
- [x] 4.2 Run `mvn clean test` — verify all existing tests still pass

## 5. Spec Sync

- [x] 5.1 Sync delta spec to main `openspec/specs/coding-standards/spec.md`
