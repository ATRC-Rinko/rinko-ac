## 1. Docker Compose Agent Init

- [x] 1.1 Add `skywalking-agent-init` container to `docker-compose.yml` — download + extract agent to shared volume
- [x] 1.2 Add `sw-agent` shared volume and mount to all 6 service containers (once defined)

## 2. Dockerfile Update

- [x] 2.1 Update all 6 Dockerfiles — copy agent jar, add `-javaagent` to ENTRYPOINT

## 3. Nacos Config

- [x] 3.1 Update `nacos-config/application-dev.yml` — add `SW_AGENT_NAME` and `SW_AGENT_COLLECTOR_BACKEND_SERVICES` env var mapping

## 4. Verification

- [x] 4.1 Run `mvn clean compile` — verify no build impact

## 5. Spec Sync

- [x] 5.1 Sync delta spec to `openspec/specs/configuration-standards/`
