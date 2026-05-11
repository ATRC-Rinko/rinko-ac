## 1. Review coding-standards

- [x] 1.1 Cross-reference `coding-standards` spec against rinko-infra source code for accuracy — verify package structure, exception classes, and naming conventions match actual code
- [x] 1.2 Cross-reference `coding-standards` spec against rinko-auth source code for accuracy — verify Kotlin naming conventions, service/controller patterns, and logging usage match actual code
- [x] 1.3 Verify `coding-standards` has no conflicting requirements with `docs/constitution.md`

## 2. Review testing-standards

- [x] 2.1 Cross-reference `testing-standards` spec against rinko-auth test code — verify Kotest StringSpec usage, Mockito mocking patterns, and test structure match actual tests
- [x] 2.2 Verify JaCoCo configuration requirements match root `pom.xml` plugin settings
- [x] 2.3 Verify TCL testing requirements match existing test scripts in `tests/auth/`

## 3. Review module-structure

- [x] 3.1 Cross-reference `module-structure` spec against actual `pom.xml` files — verify dependency declarations for rinko-infra (Java), rinko-auth (Kotlin WebFlux), and 5 skeleton modules
- [x] 3.2 Verify port assignments match those in actual `application.yml` files across all modules
- [x] 3.3 Verify graceful shutdown configuration matches Nacos `application-dev.yml`

## 4. Review api-design

- [x] 4.1 Cross-reference `api-design` spec against rinko-auth controller endpoints — verify path conventions, HTTP method usage, and `@Operation` annotation patterns
- [x] 4.2 Verify ProblemDetail builder pattern matches `rinko-infra` ProblemDetail.java implementation
- [x] 4.3 Verify OpenAPI config pattern matches rinko-auth `OpenAPIConfig.kt`

## 5. Review database-standards

- [x] 5.1 Cross-reference `database-standards` against rinko-auth Flyway migration scripts — verify table definitions, naming conventions, and constraint patterns
- [x] 5.2 Cross-reference entity class conventions against `User.kt`, `RolePermission.kt`, `OAuth2Client.kt`
- [x] 5.3 Cross-reference repository patterns against `UserRepository.kt`, `RoleRepository.kt` etc.
- [x] 5.4 Verify Druid/R2DBC dual-configuration pattern matches rinko-auth setup

## 6. Review configuration-standards

- [x] 6.1 Cross-reference `configuration-standards` against actual `application.yml` files in all 7 modules — verify Nacos import structure and absence of `bootstrap.yml`
- [x] 6.2 Cross-reference Nacos config file structure against `nacos-config/` directory contents
- [x] 6.3 Verify `CorsProperties.java` and `DruidDataSourceProperties.java` patterns match properties class requirements

## 7. Final consistency check

- [x] 7.1 Verify no internal contradictions across all 6 specs
- [x] 7.2 Verify all 6 specs together are consistent with `docs/constitution.md` (4 immutable principles)
- [x] 7.3 Verify all 6 specs are consistent with `docs/spec.md` technical stack and module boundaries
- [x] 7.4 Verify each spec file has at least one `### Requirement:` with a corresponding `#### Scenario:`
