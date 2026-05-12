## Why

SkyWalking OAP + UI 已在 `docker-compose.yml` 中部署，`rinko-infra` 中的 `TraceIdConverter` 和 `JsonEncoder` 已支持 SkyWalking traceId 注入日志。但各业务模块的 Dockerfile 尚未真正挂载 SkyWalking Java Agent，导致运行时不产生 Trace 数据，SkyWalking UI 中无链路信息。需要将 Agent 集成到每个服务容器的启动流程中。

## What Changes

- Docker Compose 添加 `skywalking-agent` 初始化容器——下载并提取 SkyWalking Java Agent 到共享卷
- 所有 6 个 Dockerfile 从共享卷复制 Agent 到镜像，更新 ENTRYPOINT 传入 `-javaagent`
- Nacos 公共配置 `application-dev.yml` 添加 SkyWalking Agent 环境变量（`SW_AGENT_NAME`、`SW_AGENT_COLLECTOR_BACKEND_SERVICES`）
- `nacos-config/application-dev.yml` 已部分配置 `skywalking.agent.*`，补充 OAP 地址映射

## Capabilities

### New Capabilities

<!-- 纯基础设施配置 -->

### Modified Capabilities

- `configuration-standards`: 补充 SkyWalking Agent 接入配置规范

## Impact

- 修改文件：
  - `docker-compose.yml` — 添加 Agent 初始化容器 + 共享卷
  - 6 个 `Dockerfile` — 复制 Agent、更新 ENTRYPOINT
  - `nacos-config/application-dev.yml` — 补充 SW Agent 环境变量
- 无代码变更、无依赖变更
- 运行时影响：每个服务启动时加载 ~30MB Agent jar，增加约 1-2 秒启动时间
