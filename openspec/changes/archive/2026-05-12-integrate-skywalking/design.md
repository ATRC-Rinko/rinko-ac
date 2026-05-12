## Context

SkyWalking OAP + UI 已在 Docker Compose 部署（端口 11800/12800/8080）。`TraceIdConverter` 通过反射获取 TraceContext，无需编译时依赖。各服务 Dockerfile 有 `ARG SKYWALKING_AGENT` 但未实际挂载 Agent jar。

## Goals / Non-Goals

**Goals:**
- 所有服务启动时自动加载 SkyWalking Java Agent
- Agent 通过 Docker Compose 共享卷分发（避免每个 Dockerfile 重复下载）
- OAP 后端地址通过环境变量注入

**Non-Goals:**
- 不修改 `TraceIdConverter`（已通过反射兼容）
- 不添加 SkyWalking 编译依赖（保持 infra 独立性）

## Decisions

### 1. Agent 分发：Docker Compose 初始化容器 + 共享卷

**决策**: `skywalking-agent-init` 容器在启动时下载 Agent tar.gz 并解压到共享卷 `/sw-agent`。各服务容器挂载该卷，Dockerfile 从 `/sw-agent/skywalking-agent.jar` 复制。

```yaml
skywalking-agent-init:
  image: alpine:3.20
  volumes:
    - sw-agent:/sw-agent
  command: >
    sh -c "wget -qO- https://dlcdn.apache.org/skywalking/java-agent/9.4.0/apache-skywalking-java-agent-9.4.0.tgz | tar xz -C /sw-agent --strip-components=1"
```

### 2. Dockerfile 简化

移除 `ARG SKYWALKING_AGENT`，直接从约定的路径复制 Agent：
```dockerfile
COPY --from=agent-init /sw-agent/skywalking-agent.jar /app/skywalking-agent.jar
ENTRYPOINT java $JAVA_OPTS -javaagent:/app/skywalking-agent.jar -jar app.jar
```

### 3. Agent 配置：环境变量

```yaml
environment:
  SW_AGENT_NAME: rinko-auth
  SW_AGENT_COLLECTOR_BACKEND_SERVICES: skywalking-oap:11800
```

## Risks / Trade-offs

- **[风险] Agent 下载失败导致所有服务无法启动** → `docker compose up` 需先完成 `agent-init`；Agent 版本固定，使用 ASF CDN
