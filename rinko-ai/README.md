# rinko-ai — AgentScope 2.0 集成模块

Rinko AI 基础设施共享库，基于 [AgentScope Java 2.0](https://github.com/agentscope-ai/agentscope-java) 提供开箱即用的 AI Agent 能力。模块定位为共享库（与 `rinko-infra` 同级），供 WebFlux 模块引用。

## 目录

- [快速开始](#快速开始)
- [配置参考](#配置参考)
- [API 使用](#api-使用)
  - [基础对话](#1-基础对话)
  - [流式对话 SSE](#2-流式对话-sse)
  - [RAG 知识库问答](#3-rag-知识库问答)
  - [多 Agent 编排](#4-多-agent-编排)
  - [自定义 Tool](#5-自定义-tool)
  - [会话记忆](#6-会话记忆)
- [提供商配置](#提供商配置)
- [与现有模块集成](#与现有模块集成)

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.rinko</groupId>
    <artifactId>rinko-ai</artifactId>
</dependency>
```

### 2. 配置

```yaml
# application.yml 或 Nacos 配置
rinko.ai:
  enabled: true
  model: dashscope:qwen-plus    # 模型字符串：provider:model-name
  agent:
    name: my-assistant
    sys-prompt: "你是一个有帮助的助手。"
    workspace: ./data/ai/workspace
```

### 3. 设置 API Key

```bash
# 根据你选择的 provider 设置对应的环境变量
export DASHSCOPE_API_KEY=sk-xxx    # DashScope
export OPENAI_API_KEY=sk-xxx       # OpenAI
export DEEPSEEK_API_KEY=sk-xxx     # DeepSeek
export ANTHROPIC_API_KEY=sk-xxx    # Anthropic
```

### 4. 注入使用

```java
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final ChatAgentService chatAgentService;

    @PostMapping("/chat")
    public Mono<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        return chatAgentService.chat(request)
                .map(ApiResponse::success);
    }
}
```

---

## 配置参考

所有配置前缀为 `rinko.ai`，支持 Nacos 动态刷新。

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `rinko.ai.enabled` | `true` | 是否启用，设为 `false` 则不会创建任何 Bean |
| `rinko.ai.model` | `dashscope:qwen-plus` | 模型字符串，格式 `provider:model-name` |
| `rinko.ai.agent.name` | `rinko-assistant` | Agent 名称（用于日志和状态存储） |
| `rinko.ai.agent.sys-prompt` | `You are a helpful AI assistant.` | 系统提示词 |
| `rinko.ai.agent.workspace` | `./data/ai/workspace` | 工作区路径（存放 AGENTS.md、会话日志等） |
| `rinko.ai.agent.compaction-trigger-messages` | `30` | 触发对话压缩的消息数阈值 |
| `rinko.ai.agent.compaction-keep-messages` | `10` | 压缩后保留的最近消息数 |
| `rinko.ai.rag.enabled` | `false` | 是否启用 RAG 知识库 |
| `rinko.ai.rag.chunk-size` | `500` | 文档分块大小（字符数） |
| `rinko.ai.rag.top-k` | `5` | 检索返回的 Top-K 文档数 |
| `rinko.ai.memory.type` | `in-memory` | 记忆存储类型：`in-memory` \| `redis` |
| `rinko.ai.memory.max-history` | `50` | 每个会话保留的最大历史条数 |

完整配置示例：

```yaml
rinko.ai:
  enabled: true
  model: deepseek:deepseek-chat
  agent:
    name: rinko-assistant
    sys-prompt: |
      你是一个 Rinko 平台的智能助手。
      你可以帮助用户：
      1. 回答问题
      2. 处理文件
      3. 执行定时任务
    workspace: /data/ai/workspace
    compaction-trigger-messages: 20
    compaction-keep-messages: 8
  rag:
    enabled: true
    chunk-size: 800
    top-k: 5
  memory:
    type: in-memory
    max-history: 100
```

---

## API 使用

### 1. 基础对话

同步式对话，等待完整回复后返回。

```java
@Autowired
private ChatAgentService chatAgentService;

public Mono<ApiResponse<ChatResponse>> handleChat(String userMessage) {
    ChatRequest request = new ChatRequest(
            userMessage,           // 用户消息
            "session-123",         // 会话 ID（支持多轮对话）
            Map.of("role", "admin") // 附加上下文（可选）
    );

    return chatAgentService.chat(request)
            .map(response -> {
                System.out.println("回复: " + response.content());
                System.out.println("会话: " + response.sessionId());
                return ApiResponse.success(response);
            });
}
```

**ChatRequest** 字段：

| 参数 | 类型 | 说明 |
|------|------|------|
| `message` | `String` | 用户消息（必填） |
| `sessionId` | `String` | 会话 ID，不传则自动生成新会话 |
| `context` | `Map<String,Object>` | 附加参数，传递给 Agent |

**ChatResponse** 字段：

| 参数 | 类型 | 说明 |
|------|------|------|
| `sessionId` | `String` | 当前会话 ID |
| `content` | `String` | 回复内容 |
| `toolCalls` | `List<ToolCallRecord>` | 工具调用记录 |
| `usage` | `TokenUsage` | Token 用量统计 |
| `timestamp` | `LocalDateTime` | 时间戳 |

### 2. 流式对话 SSE

逐 token 推送，适合 Web 前端实时展示。

```java
@GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> chatStream(@RequestParam String message) {
    ChatRequest request = new ChatRequest(message, "session-456");

    return chatAgentService.chatStream(request)
            .map(token -> ServerSentEvent.<String>builder()
                    .data(token)
                    .build());
}
```

前端消费示例（JavaScript）：

```javascript
const eventSource = new EventSource('/api/v1/ai/chat/stream?message=你好');

eventSource.onmessage = (event) => {
    // 逐 token 追加到界面
    appendToChatBox(event.data);
};

eventSource.onerror = () => {
    eventSource.close();
};
```

### 3. RAG 知识库问答

先索引文档，再基于知识库问答。

```java
@Autowired
private KnowledgeBaseService kbService;

// 步骤 1：索引文档
public Mono<Void> indexDocuments() {
    List<KnowledgeBaseService.Document> docs = List.of(
        new KnowledgeBaseService.Document(
            "Rinko 是一个基于 Spring Boot 4 的微服务平台...",
            Map.of("title", "Rinko 平台概述", "category", "platform")
        ),
        new KnowledgeBaseService.Document(
            "AgentScope 2.0 是阿里开源的企业级 AI Agent 框架...",
            Map.of("title", "AgentScope 介绍", "category", "ai")
        )
    );
    return kbService.indexBatch(docs);
}

// 步骤 2：基于知识库提问
public Mono<ApiResponse<ChatResponse>> askKnowledge(String question) {
    return kbService.ask(new ChatRequest(question))
            .map(ApiResponse::success);
}

// 流式版本
public Flux<String> askKnowledgeStream(String question) {
    return kbService.askStream(new ChatRequest(question));
}
```

**RAG 原理**：提问时自动检索相关文档片段，拼接到 System Prompt 中，让 LLM 基于参考资料回答。当前为内存实现，启用 pgvector 后可切换为语义向量检索。

### 4. 多 Agent 编排

注册多个子 Agent，由编排器分配子任务。

```java
@Autowired
private AgentOrchestrator orchestrator;

// 注册子 Agent（通常在初始化时一次性注册）
@PostConstruct
void setupSubAgents() {
    orchestrator.registerAgent("translator",
            "你是翻译专家，能将文本翻译成英文、日文、法文等多种语言。");

    orchestrator.registerAgent("summarizer",
            "你是摘要专家，能将长文本压缩为 3-5 条要点。");

    orchestrator.registerAgent("coder",
            "你是 Java 专家，能编写高质量的生产级 Java 代码。");
}

// 执行复杂任务
public Mono<ApiResponse<ChatResponse>> complexTask(String task) {
    return orchestrator.execute(task, Map.of("priority", "high"))
            .map(ApiResponse::success);
}

// 示例：主 Agent 会自动将 "翻译这份文档然后总结要点" 拆分为
// 1. translator → 翻译
// 2. summarizer → 总结
// 3. 汇总结果返回
```

### 5. 自定义 Tool

使用 `@Tool` 注解定义 LLM 可调用的函数。

```java
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * 自定义 Tool — 需要注册为 Spring Bean 或在 Toolkit 中手动注册。
 */
@Component
public class MyBusinessTool {

    @Tool(
        name = "query_order_status",
        description = "根据订单号查询订单状态",
        readOnly = true,
        concurrencySafe = true
    )
    public String queryOrderStatus(
            @ToolParam(name = "orderId", description = "订单号")
            String orderId) {
        // 实际业务逻辑：查询数据库或调用微服务
        return "订单 " + orderId + " 状态：已发货，预计 6 月 27 日送达";
    }

    @Tool(
        name = "send_notification",
        description = "向指定用户发送通知"
    )
    public String sendNotification(
            @ToolParam(name = "userId", description = "用户 ID")
            String userId,
            @ToolParam(name = "message", description = "通知内容")
            String message) {
        // 调用 rinko-notify 服务
        return "通知已发送给用户 " + userId;
    }
}
```

注册方式（二选一）：

```java
// 方式 A：Spring Bean 自动发现
@Component
public class MyToolConfig {
    @Bean
    public Toolkit customToolkit(MyBusinessTool myBusinessTool) {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(myBusinessTool);
        return toolkit;
    }
}

// 方式 B：直接注入到 AutoConfiguration 已有的 Toolkit
@Bean
public Toolkit agentScopeToolkit(MyBusinessTool myBusinessTool) {
    Toolkit toolkit = new Toolkit();
    toolkit.registerTool(new DateTimeTool());
    toolkit.registerTool(new JsonTool());
    toolkit.registerTool(myBusinessTool);  // 你的自定义 Tool
    return toolkit;
}
```

### 6. 会话记忆

管理对话历史和上下文。

```java
@Autowired
private ConversationMemory memory;

// 手动追加（通常由 Agent 自动管理，无需手动调用）
memory.append("session-789", "user", "你好");
memory.append("session-789", "assistant", "你好！有什么可以帮助你的？");

// 获取最近 10 条历史
memory.getRecentHistory("session-789", 10)
        .subscribe(msgs -> msgs.forEach(m ->
            System.out.println(m.role() + ": " + m.content())));

// 清除会话
memory.clear("session-789").subscribe();
```

---

## 提供商配置

### DashScope（阿里云通义千问）

```bash
export DASHSCOPE_API_KEY=sk-xxx
```

```yaml
rinko.ai.model: dashscope:qwen-plus
```

适合国内部署，与 Nacos / Seata 同属阿里云生态。

### DeepSeek

```bash
export DEEPSEEK_API_KEY=sk-xxx
```

```yaml
rinko.ai.model: deepseek:deepseek-chat
```

DeepSeek 使用 OpenAI 兼容协议，自动配置 `baseUrl=https://api.deepseek.com`。

### OpenAI

```bash
export OPENAI_API_KEY=sk-xxx
```

```yaml
rinko.ai.model: openai:gpt-4o
```

如需自定义 baseUrl（代理或兼容 API）：

```yaml
# 通过环境变量设置
export OPENAI_BASE_URL=https://your-proxy.com
```

### Anthropic Claude

```bash
export ANTHROPIC_API_KEY=sk-xxx
```

```yaml
rinko.ai.model: anthropic:claude-sonnet-4-6
```

### 运行时切换

模型字符串支持动态切换（配合 Nacos 可实现不停机切换）：

```java
// ApplicationRunner 中读取配置决定用哪个模型
@Value("${rinko.ai.model}")
private String model;
```

---

## 与现有模块集成

### 在 rinko-auth 中使用

```java
// 在 pom.xml 添加依赖
// <dependency>
//     <groupId>com.rinko</groupId>
//     <artifactId>rinko-ai</artifactId>
// </dependency>

@RestController
@RequestMapping("/api/v1/auth/ai")
@RequiredArgsConstructor
public class AuthAiController {

    private final ChatAgentService chatAgentService;

    @PostMapping("/assistant")
    @PreAuthorize("hasRole('USER')")
    public Mono<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        // 自动从 SecurityContext 获取用户 ID 作为会话隔离
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    String userId = ctx.getAuthentication().getName();
                    return new ChatRequest(request.message(), userId, null);
                })
                .flatMap(chatAgentService::chat)
                .map(ApiResponse::success);
    }
}
```

### 在 rinko-gateway 中使用

作为网关层的 AI 路由，对请求做智能预处理或路由决策：

```java
@Component
class AiRoutingFilter(private val chatService: ChatAgentService) : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        return Mono.just(exchange)
            .flatMap {
                // 对请求路径做智能分析，决定路由策略
                chain.filter(exchange)
            }
    }

    override fun getOrder(): Int = -50
}
```

### 在 rinko-scheduler 中使用

定时任务中调用 AI 生成报告：

```java
@Component
public class AiReportJob implements Job {

    @Autowired
    private ChatAgentService chatAgentService;

    @Override
    public void execute(JobExecutionContext context) {
        ChatRequest request = new ChatRequest(
                "请根据今天的系统日志生成一份简要报告",
                "report-" + LocalDate.now(),
                Map.of("source", "scheduler")
        );
        ChatResponse response = chatAgentService.chat(request).block();
        // 发送到通知模块
    }
}
```

---

## 生产部署

单机开发（默认）只适合本地调试。多副本部署必须换掉以下组件：

| 维度 | 开发默认 | 生产替换 |
|------|----------|----------|
| AgentStateStore | JsonFileAgentStateStore（本地 JSON） | RedisAgentStateStore / MysqlAgentStateStore |
| Filesystem | LocalFilesystem（本机磁盘） | RemoteFilesystemSpec（共享 KV）或 SandboxFilesystemSpec |
| Sandbox 快照 | NoopSnapshotSpec（容器销毁即丢） | OssSnapshotSpec / RedisSnapshotSpec |
| Skill 来源 | workspace/skills/ | GitSkillRepository / MysqlSkillRepository |
| 观测 | 无 tracing | OtelTracingMiddleware + OpenTelemetry |

### 一键分布式配置

引入扩展依赖后，用 DistributedStore 一键配置所有分布式组件：

```java
import io.agentscope.extensions.redis.RedisDistributedStore;
import io.agentscope.harness.agent.DistributedStore;
import redis.clients.jedis.JedisPooled;

// 应用启动时创建一次
JedisPooled jedis = new JedisPooled(System.getenv("REDIS_URI"));
DistributedStore store = RedisDistributedStore.fromJedis(jedis);

HarnessAgent agent = HarnessAgent.builder()
    .name("production-agent")
    .model("dashscope:qwen-plus")
    .workspace(workspace)
    .distributedStore(store)        // 自动注入 stateStore + baseStore + snapshotSpec + executionGuard
    .filesystem(new DockerFilesystemSpec()
            .image("python:3.12-slim")
            .isolationScope(IsolationScope.USER))
    .compaction(CompactionConfig.builder()
            .triggerMessages(50)
            .keepMessages(20)
            .build())
    .build();
```

使用 `distributedStore(...)` 后，以下组件自动注入：

- **AgentStateStore**：对话上下文、压缩摘要跨副本恢复
- **BaseStore**：MEMORY.md、memory/、skills/ 等 workspace 路径共享 KV
- **SandboxSnapshotSpec**：沙箱 workspace 快照持久化
- **SandboxExecutionGuard**：跨节点 sandbox 执行串行化

### 多副本 + 沙箱模板

```java
DistributedStore store = RedisDistributedStore.fromJedis(jedis);

// Docker 沙箱 + Redis 状态存储 + OSS 快照（大对象）
HarnessAgent agent = HarnessAgent.builder()
    .name("coding-agent")
    .model("deepseek:deepseek-chat")
    .workspace(Paths.get("/var/agentscope/workspace"))
    .distributedStore(store)
    .filesystem(new DockerFilesystemSpec()
            .image("ubuntu:24.04")
            .isolationScope(IsolationScope.SESSION))  // 每会话独立沙箱
    .compaction(CompactionConfig.builder()
            .triggerMessages(50)
            .keepMessages(20)
            .build())
    .skillRepository(MysqlSkillRepository.builder(dataSource)
            .writeable(false)              // 只读分发
            .build())
    .middlewares(List.of(new OtelTracingMiddleware()))
    .build();

// HTTP handler 中 —— 每次调用传入 RuntimeContext
agent.call(msg, RuntimeContext.builder()
        .userId(tenantId + ":" + userId)
        .sessionId(agentId + ":" + sessionId)
        .build()).block();
```

### OSS 混合存储

避免把大文件写 Redis：

```java
DistributedStore redisStore = RedisDistributedStore.fromJedis(jedis);
DistributedStore ossStore = OssDistributedStore.create(
        ossClient, "snapshot-bucket", "prod/");

DistributedStore store = DistributedStore.builder()
        .agentStateStore(redisStore.agentStateStore())
        .baseStore(redisStore.baseStore())
        .sandboxSnapshotSpec(ossStore.sandboxSnapshotSpec())   // 大对象走 OSS
        .sandboxExecutionGuard(redisStore.sandboxExecutionGuard())
        .build();
```

### Filesystem 三种模式

| 模式 | 配置 | shell | 适用 |
|------|------|-------|------|
| 本机 | 默认（不配） | ✅ | 单进程 / 信任环境 |
| 共享存储 | RemoteFilesystemSpec(store) + IsolationScope.USER | ❌ | 多副本共享长期记忆 |
| 沙箱 | DockerFilesystemSpec / K8sFilesystemSpec | ✅ 沙箱内 | 不可信代码 / 多用户隔离 |

### 生产 checklist

1. ✅ `userId` + `sessionId` 二元组寻址 —— 防止跨用户串读
2. ✅ `RuntimeContext` 每次 call 都传入 —— 不传则共享 defaultSessionId
3. ✅ 定好 `IsolationScope` 再上线 —— 改了等于换命名空间，旧数据不迁移
4. ✅ Skill 用 MysqlSkillRepository(writeable=false) —— 平台集中治理，agent 只读
5. ✅ 开 `enableSkillManageTool` 时必须配 `enableSkillPromotionGate` —— 禁止 autoPromote
6. ✅ `OtelTracingMiddleware` + OpenTelemetry —— 可观测
7. ✅ K8s 多副本里绝不用本地 `JsonFileAgentStateStore` —— builder 会直接抛异常

### 本模块内置配置

模块通过 `AiProperties` 提供声明式配置（无需写 Java 代码）：

```yaml
rinko.ai:
  distributed:
    store-type: redis             # none | redis | mysql
    redis:
      uri: redis://prod-redis:6379
      key-prefix: myapp:
  filesystem:
    mode: remote                  # local | remote | sandbox
    isolation-scope: user
    anonymous-user-id: _default
  sandbox:
    type: docker
    image: ubuntu:24.04
```

引入 `agentscope-extensions-redis` 依赖后，设置 `rinko.ai.distributed.store-type=redis` 即可自动创建 `DistributedStore` Bean。生产项目建议直接在 `@Configuration` 中显式构建 `HarnessAgent`，完整控制所有参数。

---

## 扩展指南

### 替换 RAG 为 pgvector

```java
@Component
@ConditionalOnProperty(prefix = "rinko.ai.rag", name = "vector-store", havingValue = "pgvector")
public class PgVectorKnowledgeBaseService implements KnowledgeBaseService {
    // 实现基于 PostgreSQL pgvector 的向量检索
    // 使用 R2DBC 或 JDBC 连接数据库
    // 调用 embedding API 将文本转为向量
}
```

### 分布式记忆（Redis）

```java
@Component
@ConditionalOnProperty(prefix = "rinko.ai.memory", name = "type", havingValue = "redis")
public class RedisConversationMemory implements ConversationMemory {
    // 基于 Redis 的分布式会话记忆
    // 支持多实例共享同一会话的对话历史
}
```

---

## 常见问题

**Q: 启用模块后如何验证配置正确？**

```bash
# 检查 Bean 是否创建
curl http://localhost:8081/actuator/beans | grep -i agent

# 或者查看启动日志
# 应有: "Creating HarnessAgent: name=rinko-assistant, model=dashscope:qwen-plus"
```

**Q: 如何禁用 AI 能力？**

```yaml
rinko.ai.enabled: false
```

设置后不会创建任何 AgentScope Bean，对性能零影响。

**Q: 多用户并发安全吗？**

安全。HarnessAgent 是单例，通过 `RuntimeContext` 的 `(userId, sessionId)` 自动隔离上下文，同一 `sessionId` 自动串行化，不同 session 完全并行。

**Q: Agent 状态存在哪里？**

默认存储在 `~/.agentscope/state/<agentName>/` 下（与工作区分离）。进程重启、sessionId 相同，对话历史自动恢复。生产环境建议切换为 Redis。
