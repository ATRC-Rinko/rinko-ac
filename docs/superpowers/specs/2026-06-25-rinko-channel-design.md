# rinko-channel: Multi-Platform Bot Channel Module

**Date:** 2026-06-25
**Status:** Draft

---

## 1. Overview

`rinko-channel` is a new module family that provides a unified abstraction layer for building bots across multiple chat/IM platforms — QQ, WeChat, DingTalk, Discord, and future platforms. It handles both inbound message reception and outbound message delivery through a common event-driven programming model.

### 1.1 Goals

- Provide a single, consistent `ChannelBot` interface that all platform adapters implement
- Define a RichMessage intermediate representation (block-based, similar to Slack Block Kit) for cross-platform message compatibility
- Support AI-powered chatbot responses via `AiBridge` — transparently using rinko-ai locally or remotely
- Persist all messages with a hot/warm/cold tiered storage strategy and AI-driven summarization
- Delegate compression scheduling to the existing `rinko-scheduler` module
- Allow runtime lifecycle management (start/stop/status) of individual platform bots

### 1.2 Non-Goals

- GUI / management dashboard (REST API only)
- Real-time message sync between platforms (cross-platform relay)
- End-to-end encryption of messages

---

## 2. Module Structure

```
rinko-channel/                  ← Common API module (interfaces, models, events, persistence)
rinko-channel-qq/               ← QQ bot (WebSocket + passive reply), standalone Spring Boot
rinko-channel-wechat/           ← WeChat Official Account bot, standalone Spring Boot
rinko-channel-dingtalk/         ← DingTalk bot (Webhook), standalone Spring Boot
rinko-channel-discord/          ← Discord bot (Gateway/WebSocket), standalone Spring Boot
```

Each platform module depends on `rinko-channel`, plus its platform-specific SDK. Each is deployed independently.

### 2.1 Dependency Graph

```
rinko-channel-qq ──┐
rinko-channel-wechat ─┤
rinko-channel-dingtalk ├──► rinko-channel ──► rinko-infra
rinko-channel-discord ─┘         │
                                 ├──► rinko-ai (optional, @ConditionalOnBean)
                                 └──► (called by) rinko-scheduler (for compression)
```

---

## 3. Common API (`rinko-channel`)

### 3.1 Package Layout

```
com.rinko.channel
├── bot/
│   ├── ChannelBot                 ← Unified interface
│   ├── AbstractChannelBot         ← Template base class (event dispatch, retry, rate-limit)
│   └── BotContext                 ← Runtime context (platform, botId, config snapshot)
├── event/
│   ├── ChannelEvent               ← Event base class
│   ├── MessageReceivedEvent       ← Inbound message from user
│   ├── MessageSentEvent          ← Outbound message confirmation
│   ├── UserJoinedEvent           ← User joined channel/group
│   ├── UserLeftEvent             ← User left channel/group
│   └── ChannelLifecycleEvent     ← Connected / Disconnected / Reconnecting
├── message/
│   ├── RichMessage               ← Block-based intermediate representation
│   ├── Block                     ← Block base (type discriminator)
│   ├── TextBlock                 ← Plain text or markdown text
│   ├── ImageBlock                ← Image URL / base64
│   ├── ButtonBlock               ← Interactive button
│   ├── ActionRowBlock            ← Horizontal row of buttons
│   ├── EmbedBlock                ← Rich embed (title, description, fields, color)
│   ├── SectionBlock              ← Text + optional accessory
│   ├── DividerBlock              ← Visual divider
│   └── MessageConverter<T>       ← Platform message <-> RichMessage bidirectional
├── user/
│   ├── UnifiedUser               ← Cross-platform user identity
│   ├── PlatformUserId            ← (platformType, platformUserId) tuple
│   └── UserMappingService        ← Maps PlatformUserId <-> UnifiedUser
├── ai/
│   ├── AiBridge                   ← Abstract AI integration
│   ├── LocalAiBridge              ← Direct dependency on rinko-ai ChatAgentService
│   └── RemoteAiBridge             ← HTTP client to rinko-ai service
├── config/
│   ├── ChannelProperties          ← @ConfigurationProperties("rinko.channel")
│   └── ChannelAutoConfiguration   ← Auto-config (AiBridge, persistence, converters)
├── persistence/
│   ├── entity/
│   │   ├── ChannelMessageHistory  ← Raw message entity
│   │   └── ConversationSummary    ← AI-compressed summary entity
│   ├── ChannelMessageHistoryMapper
│   ├── ConversationSummaryMapper
│   ├── MessageCompressionTask     ← Compression execution logic (bean for rinko-scheduler)
│   └── MessagePersistenceService  ← CRUD + query with hot/warm/cold routing
├── management/
│   ├── ChannelManager             ← Runtime lifecycle (start/stop/status per platform)
│   ├── ChannelHealthIndicator     ← Spring Boot health indicator
│   └── controller/
│       └── ChannelManageController ← REST API for runtime management
└── model/
    ├── dto/
    │   ├── ChannelStatusDto
    │   ├── SendMessageRequest
    │   └── SendMessageResponse
    └── vo/
        ├── ChannelStatusVO
        └── MessageHistoryVO
```

### 3.2 `ChannelBot` Interface

```java
public interface ChannelBot {

    /** Platform identifier: "QQ", "WECHAT", "DINGTALK", "DISCORD" */
    String getPlatform();

    /** Called by the adapter when a platform event arrives */
    void onEvent(ChannelEvent event);

    /** Send a RichMessage to a specific user/channel on this platform */
    CompletableFuture<SendResult> send(PlatformUserId recipient, RichMessage message);

    /** Start the bot (connect to platform, begin receiving events) */
    void start(BotContext context);

    /** Graceful shutdown */
    void stop();

    /** Current connection status */
    ChannelStatus getStatus();
}
```

### 3.3 `AbstractChannelBot` Template

Provides base implementations for:
- Event type dispatching (`onEvent` → `onMessageReceived`, `onUserJoined`, etc.)
- Rate limiting (token bucket per platform)
- Retry with exponential backoff on send failures
- Metrics collection (messages received/sent, latency, errors)
- Lifecycle state machine (INIT → CONNECTING → CONNECTED → DISCONNECTING → DISCONNECTED)

### 3.4 `ChannelEvent` Hierarchy

```java
public sealed interface ChannelEvent permits
    MessageReceivedEvent,
    MessageSentEvent,
    UserJoinedEvent,
    UserLeftEvent,
    ChannelLifecycleEvent {

    String eventId();          // UUID
    Instant timestamp();       // When the event was created
    String platformType();     // QQ, WECHAT, etc.
    String botId();            // Which bot instance received this
}
```

`MessageReceivedEvent` carries:
- `PlatformUserId sender`
- `RichMessage message` (converted from platform-native format)
- `String channelId` (group/DM identifier within the platform)
- `Map<String, Object> metadata` (platform-specific extras)

---

## 4. RichMessage Model

### 4.1 Design

A block-based intermediate format inspired by Slack Block Kit. Each platform's `MessageConverter<T>` handles bidirectional conversion between native platform messages and RichMessage blocks.

```java
public class RichMessage {
    private String messageId;
    private List<Block> blocks;
    private String fallbackText;    // For platforms that don't support rich blocks
    private Map<String, Object> metadata;
}

public sealed interface Block permits
    TextBlock, ImageBlock, ButtonBlock, ActionRowBlock,
    EmbedBlock, SectionBlock, DividerBlock {

    BlockType type();    // Enum discriminator for JSON serialization
}
```

### 4.2 Block Types

| Block | Fields | Usage |
|-------|--------|-------|
| `TextBlock` | `text`, `textStyle` (PLAIN/MARKDOWN/BOLD/ITALIC) | Text content |
| `ImageBlock` | `imageUrl`, `altText`, `width`, `height` | Images |
| `ButtonBlock` | `text`, `actionId`, `style` (PRIMARY/DANGER/DEFAULT), `url` | Interactive buttons |
| `ActionRowBlock` | `List<ButtonBlock>` | Horizontal button row |
| `EmbedBlock` | `title`, `description`, `url`, `color`, `fields`, `thumbnail` | Rich embeds |
| `SectionBlock` | `text`, `accessory` (optional Button/Image) | Text + optional widget |
| `DividerBlock` | (none) | Visual separator |

### 4.3 Conversion Contract

```java
public interface MessageConverter<T> {
    /** Platform-native type this converter handles */
    Class<T> getNativeType();

    /** Convert platform-native message to RichMessage */
    RichMessage toRichMessage(T nativeMessage);

    /** Convert RichMessage to platform-native payload */
    T toNativeMessage(RichMessage richMessage);

    /** Whether this platform supports a given Block type natively */
    boolean supportsBlock(BlockType type);
}
```

Each platform module provides converters for its native message types. Unsupported blocks degrade gracefully (e.g., `ImageBlock` → URL text for platforms without image rendering).

---

## 5. AI Integration

### 5.1 `AiBridge`

```java
public interface AiBridge {
    /** Given conversation context + user message, generate a reply */
    CompletableFuture<RichMessage> generateReply(
        UnifiedUser user,
        RichMessage currentMessage,
        List<ChannelMessageHistory> recentHistory,
        List<ConversationSummary> summaries
    );

    /** Check if AI is available */
    boolean isAvailable();
}
```

### 5.2 Implementation Selection

```java
@Configuration
@ConditionalOnBean(ChatAgentService.class)
class LocalAiBridge implements AiBridge { ... }

@Configuration
@ConditionalOnMissingBean(ChatAgentService.class)
class RemoteAiBridge implements AiBridge { ... }
```

- `LocalAiBridge` — directly calls `ChatAgentService.chat()` from rinko-ai. Used when the channel module has rinko-ai on its classpath.
- `RemoteAiBridge` — HTTP client calling `rinko-ai` via its REST API. Used when the channel module runs standalone.

---

## 6. Unified User Model

### 6.1 Entities

```java
public class UnifiedUser {
    private Long id;                      // Internal unified user ID
    private List<PlatformUserId> linkedIdentities;
    private String defaultPlatformType;   // Preferred platform
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public record PlatformUserId(
    String platformType,    // "QQ", "WECHAT", "DINGTALK", "DISCORD"
    String platformUserId   // Platform-native ID
) {}
```

### 6.2 `UserMappingService`

```java
public interface UserMappingService {
    /** Find unified user by a platform identity; create if not exists */
    UnifiedUser resolveUser(PlatformUserId platformId);

    /** Link a new platform identity to an existing unified user */
    void linkIdentity(Long unifiedUserId, PlatformUserId newPlatformId);

    /** Find by any linked platform identity */
    Optional<UnifiedUser> findByPlatformUserId(PlatformUserId platformId);
}
```

This allows a user chatting on QQ and later switching to WeChat to maintain a continuous conversation context — the `AiBridge` sees the same `UnifiedUser` regardless of which platform the current message arrived from.

---

## 7. Message Persistence & Compression

### 7.1 Three-Tier Storage

| Tier | Time Range | Storage | Purpose |
|------|-----------|---------|---------|
| **Hot** | Last 1 hour | In-memory (Caffeine cache) | Current conversation context |
| **Warm** | 1 hour ~ 7 days | Database (raw messages) | Recent history for review/query |
| **Cold** | > 7 days | AI-compressed summaries in DB; original messages archived or deleted | Long-term memory |

### 7.2 Entities

```java
public class ChannelMessageHistory {
    private Long id;
    private PlatformUserId sender;
    private PlatformUserId recipient;       // Bot that received it
    private String channelId;              // Group/DM ID
    private String direction;              // INBOUND or OUTBOUND
    private String richMessageJson;        // Serialized RichMessage
    private String platformType;
    private boolean compressed;            // Whether already summarized
    private Long summaryId;                // FK → ConversationSummary
    private LocalDateTime createdAt;
}

public class ConversationSummary {
    private Long id;
    private Long unifiedUserId;
    private String platformType;
    private String summaryText;            // AI-generated compressed summary
    private Integer originalMessageCount;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private LocalDateTime compressedAt;
}
```

### 7.3 Compression Execution

```java
@Component("messageCompressionTask")
public class MessageCompressionTask {

    /** Called by rinko-scheduler's BeanJobExecutor on a cron schedule */
    public String compress() {
        // 1. Query messages older than warm threshold (7d), not yet compressed
        // 2. Group by (unifiedUserId, platformType)
        // 3. For each group:
        //    a. Feed batch to AiBridge for summarization
        //    b. Save ConversationSummary
        //    c. Mark original messages as compressed=true
        //    d. If retention expired (90d), archive/delete originals
        // 4. Return summary: "Compressed X groups, Y messages"
    }
}
```

### 7.4 Scheduling via rinko-scheduler

The compression is NOT self-scheduled. Instead, a `BEAN` type job is registered in rinko-scheduler:

```json
{
  "type": "BEAN",
  "config": {
    "beanName": "messageCompressionTask",
    "methodName": "compress"
  }
}
```

Scheduling (cron, retries, etc.) is entirely managed by rinko-scheduler's Quartz infrastructure.

### 7.5 Configuration

```yaml
rinko.channel.persistence:
  compression:
    enabled: true
    hot-window: 1h
    warm-window: 7d
    retention:
      raw-messages-days: 90
      summary-forever: true
```

---

## 8. Runtime Management

### 8.1 `ChannelManager`

```java
public interface ChannelManager {
    /** List all registered bots and their statuses */
    List<ChannelStatusVO> listBots();

    /** Start a specific bot */
    void startBot(String platform, String botId);

    /** Stop a specific bot */
    void stopBot(String platform, String botId);

    /** Get detailed status of a single bot */
    ChannelStatusVO getBotStatus(String platform, String botId);
}
```

### 8.2 REST API

```
GET    /api/channel/bots              → List all bots with status
POST   /api/channel/bots/{id}/start   → Start a bot
POST   /api/channel/bots/{id}/stop    → Stop a bot
GET    /api/channel/bots/{id}/status  → Get detailed status
```

### 8.3 Configuration

```yaml
rinko.channel:
  bots:
    qq:
      enabled: true
      bot-id: "${QQ_BOT_ID}"
      # platform-specific config...
    wechat:
      enabled: true
      app-id: "${WECHAT_APP_ID}"
      # ...
    dingtalk:
      enabled: false
    discord:
      enabled: false
```

Startup behavior follows the existing pattern: `@ConditionalOnProperty` gates each platform adapter. Runtime management API allows toggling without restart.

---

## 9. Platform Adapter Patterns

Each platform module follows the same internal structure:

```
rinko-channel-{platform}/src/main/java/com/rinko/channel/{platform}/
├── RinkoChannel{Platform}Application.java    ← Spring Boot entry point
├── adapter/
│   └── {Platform}Adapter.java               ← WebSocket/Webhook listener, calls ChannelBot.onEvent()
├── converter/
│   └── {Platform}MessageConverter.java      ← Implements MessageConverter<NativeType>
├── bot/
│   └── {Platform}ChannelBot.java            ← Extends AbstractChannelBot
├── config/
│   └── {Platform}Properties.java            ← @ConfigurationProperties("rinko.channel.{platform}")
└── controller/
    └── {Platform}WebhookController.java      ← HTTP webhook endpoint (if applicable)
```

### 9.1 Per-Platform Connection Details

| Platform | Inbound Transport | Outbound Transport | Notes |
|----------|------------------|-------------------|-------|
| **QQ** | WebSocket (official bot SDK) | REST API | Passive reply mode for message events |
| **WeChat** | HTTP callback (Official Account) | REST API (access_token) | XML message format; need ICP-filed server |
| **DingTalk** | HTTP Webhook | REST API | Application bot model; signed callbacks |
| **Discord** | Gateway WebSocket | REST API (slash commands) | Gateway intents-based event subscription |

Each adapter handles its transport protocol internally and translates to `ChannelEvent` before calling `ChannelBot.onEvent()`.

---

## 10. Error Handling

### 10.1 Per-Message Isolation

A failure processing one message MUST NOT affect other messages. `AbstractChannelBot.onEvent()` wraps each event handler in try/catch.

### 10.2 Send Failures

Failed outbound sends are retried up to 3 times with exponential backoff (1s, 2s, 4s). After exhaustion, the message is persisted with `status=FAILED` and an error is logged.

### 10.3 Connection Failures

Platform adapters implement reconnection with backoff (1s, 2s, 4s, 8s, 16s, 32s, capped at 60s). During disconnection, incoming messages are naturally lost (the platform queues them or returns errors to the sender based on platform behavior). Status transitions are emitted as `ChannelLifecycleEvent`.

### 10.4 AI Failures

If `AiBridge.generateReply()` fails or times out, the bot falls back to a configurable static message (e.g., "Sorry, I'm having trouble thinking right now. Please try again later.") or no reply, depending on configuration.

---

## 11. Testing Strategy

### 11.1 Unit Tests

- `MessageConverter` round-trip tests: native → RichMessage → native (lossless for supported blocks)
- `AbstractChannelBot` event dispatch and lifecycle state machine
- `MessageCompressionTask` logic with mocked persistence layer

### 11.2 Integration Tests

- `AiBridge` with started/not-started `ChatAgentService`
- `UserMappingService` with real database
- Platform webhook controller with Spring MockMvc

### 11.3 Per-Platform Tests

- Each platform module should have at least one integration test that exercises the full flow: receive native payload → convert → dispatch → AI reply → convert back → verify outbound payload

---

## 12. Open Questions & Future Work

1. **Cross-platform group chat bridging** — syncing messages across platforms (e.g., QQ group + Discord channel). Out of scope for v1.
2. **Voice message support** — some platforms support voice snippets. RichMessage could gain an `AudioBlock` in the future.
3. **Rate limit per platform** — each platform has its own API rate limits; `AbstractChannelBot` rate limiter should be configurable per platform.
4. **Message deduplication** — webhook redelivery is common; a short-term dedup cache (based on platform-assigned message ID) would prevent double-processing.
