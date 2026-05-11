## 1. Configure kotlin-allopen in root POM

- [x] 1.1 Add `kotlin-allopen` compile dependency to `kotlin-maven-plugin` in root `pom.xml` `<pluginManagement>` — groupId `org.jetbrains.kotlin`, artifactId `kotlin-allopen`, version `${kotlin-maven-plugin.version}`
- [x] 1.2 Add `<pluginOptions>` with 6 Spring annotations in `kotlin-maven-plugin` configuration in root `pom.xml` — `@Component`, `@Service`, `@Repository`, `@Transactional`, `@Configuration`, `@RestController`
- [x] 1.3 Add `kotlin-allopen` version property `<kotlin-allopen.version>2.1.0</kotlin-allopen.version>` to root `pom.xml` properties (or reuse `kotlin-maven-plugin.version`)

## 2. Verification

- [x] 2.1 Run `mvn clean compile` on `rinko-auth` — verify compilation succeeds with allopen plugin active
- [x] 2.2 Run `mvn clean compile` on `rinko-gateway` — verify compilation succeeds with allopen plugin active
- [x] 2.3 Run `mvn clean test` on `rinko-auth` — verify all existing tests still pass

## 3. Update spec

- [x] 3.1 Sync the delta spec from `openspec/changes/add-kotlin-allopen/specs/coding-standards/` to main `openspec/specs/coding-standards/spec.md` — append the new "Kotlin All-Open Compiler Plugin" requirement
