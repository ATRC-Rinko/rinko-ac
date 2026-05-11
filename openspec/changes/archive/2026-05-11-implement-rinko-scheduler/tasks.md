## 1. Maven Dependencies

- [x] 1.1 Add `spring-boot-starter-data-jdbc`, `spring-boot-starter-quartz`, `spring-cloud-starter-bus-amqp` to `rinko-scheduler/pom.xml`
- [x] 1.2 Add `springdoc-openapi-starter-webmvc-ui` to `rinko-scheduler/pom.xml`

## 2. Configuration & Infrastructure

- [x] 2.1 Create `SchedulerProperties.java` — thread pool size, max retries, retry backoff
- [x] 2.2 Create Flyway `V1__create_scheduler_jobs.sql`
- [x] 2.3 Create Flyway `V2__create_scheduler_executions.sql`
- [x] 2.4 Create Flyway `V3__create_scheduler_dependencies.sql`
- [x] 2.5 Create Flyway `V4__quartz_tables.sql` — PostgreSQL Quartz DDL (11 tables)

## 3. Entity & Repository

- [x] 3.1 Create `SchedulerJob.java` + MyBatis mapper
- [x] 3.2 Create `SchedulerExecution.java` + MyBatis mapper
- [x] 3.3 Create `SchedulerDependency.java` + MyBatis mapper

## 4. Job Executors

- [x] 4.1 Create `JobExecutor.java` interface
- [x] 4.2 Create `HttpJobExecutor.java` — RestTemplate
- [x] 4.3 Create `ShellJobExecutor.java` — ProcessBuilder
- [x] 4.4 Create `BeanJobExecutor.java` — Spring context reflection

## 5. Service Layer

- [x] 5.1 Create `SchedulerService.java` — job CRUD, trigger, pause, resume, DAG dependency management
- [x] 5.2 Create `QuartzJobBeanImpl.java` — Quartz `Job` implementation, delegates to `JobExecutor`, handles retry, records execution, triggers DAG downstream

## 6. Controller Layer

- [x] 6.1 Create `SchedulerController.java` — job CRUD + trigger + pause + list executions
- [x] 6.2 Create `DagController.java` — add/remove dependencies, list downstream

## 7. Application Entry Point

- [x] 7.1 Create `RinkoSchedulerApplication.java` — `@SpringBootApplication` + `@EnableDiscoveryClient` + `@EnableDruid`

## 8. Verification

- [x] 8.1 Run `mvn clean compile` on `rinko-scheduler` — verify compilation succeeds

## 9. Spec Sync

- [x] 9.1 Sync 3 new specs to `openspec/specs/` — `scheduler-job`, `scheduler-dag`, `scheduler-execution`
