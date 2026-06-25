# rinko-channel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the rinko-channel module family — a common API module (`rinko-channel`) with unified bot/channel abstractions, plus four standalone platform modules (QQ, WeChat, DingTalk, Discord) that can both receive messages from and send messages to their respective platforms, integrating with rinko-ai for intelligent replies.

**Architecture:** `rinko-channel` is a shared JAR (not a runnable service) defining interfaces (`ChannelBot`, `AiBridge`, `MessageConverter`), models (`RichMessage`, `ChannelEvent`, `UnifiedUser`), and persistence infrastructure. Each platform module is an independently deployable Spring Boot service that implements the shared interfaces using platform-specific SDKs. The compression task is a Spring Bean in `rinko-channel` invoked by `rinko-scheduler`'s `BeanJobExecutor`.

**Tech Stack:** Java 21, Spring Boot 4.0.5, Spring Cloud 2025.1.0, MyBatis-Plus 3.5.16, Caffeine 3.1.8, Lombok 1.18.38, SLF4J, PostgreSQL, Kotlin 2.1.0

---

## File Structure Map

```
rinko-channel/                                     ← Common API JAR (not runnable)
  pom.xml
  src/main/java/com/rinko/channel/
    bot/ChannelBot.java                            ← Unified bot interface
    bot/AbstractChannelBot.java                    ← Template base class
    bot/BotContext.java                            ← Runtime context record
    event/ChannelEvent.java                        ← Sealed event interface
    event/MessageReceivedEvent.java                ← Inbound message event
    event/MessageSentEvent.java                    ← Outbound confirmation event
    event/UserJoinedEvent.java                     ← User join event
    event/UserLeftEvent.java                       ← User leave event
    event/ChannelLifecycleEvent.java               ← Connection state event
    message/Block.java                             ← Sealed block interface
    message/BlockType.java                         ← Block type enum
    message/RichMessage.java                       ← Block-based message model
    message/TextBlock.java                         ← Text block
    message/ImageBlock.java                        ← Image block
    message/ButtonBlock.java                       ← Button block
    message/ActionRowBlock.java                    ← Button row block
    message/EmbedBlock.java                        ← Rich embed block
    message/SectionBlock.java                      ← Section block
    message/DividerBlock.java                      ← Divider block
    message/MessageConverter.java                  ← Bidirectional converter interface
    user/UnifiedUser.java                          ← Cross-platform user entity
    user/PlatformUserId.java                       ← Platform identity record
    user/UserMappingService.java                   ← Identity mapping service
    ai/AiBridge.java                               ← AI integration interface
    ai/LocalAiBridge.java                          ← Direct rinko-ai call
    ai/RemoteAiBridge.java                         ← HTTP client to rinko-ai
    config/ChannelProperties.java                  ← @ConfigurationProperties
    config/ChannelAutoConfiguration.java           ← Auto-configuration
    persistence/entity/ChannelMessageHistory.java  ← Raw message entity
    persistence/entity/ConversationSummary.java    ← Compressed summary entity
    persistence/ChannelMessageHistoryMapper.java   ← MyBatis-Plus mapper
    persistence/ConversationSummaryMapper.java     ← MyBatis-Plus mapper
    persistence/MessagePersistenceService.java     ← Hot/warm/cold routing
    persistence/MessageCompressionTask.java        ← Compression bean for scheduler
    management/ChannelManager.java                 ← Runtime lifecycle
    management/ChannelHealthIndicator.java         ← Health check
    management/controller/ChannelManageController.java ← REST API
    model/dto/ChannelStatusDto.java
    model/dto/SendMessageRequest.java
    model/dto/SendMessageResponse.java
    model/vo/ChannelStatusVO.java
    model/vo/MessageHistoryVO.java
  src/test/java/com/rinko/channel/
    message/RichMessageConversionTest.java
    bot/AbstractChannelBotTest.java
    persistence/MessageCompressionTaskTest.java

rinko-channel-qq/                                  ← QQ bot (runnable Spring Boot)
  pom.xml
  src/main/java/com/rinko/channel/qq/
    RinkoChannelQqApplication.java
    adapter/QqAdapter.java
    converter/QqMessageConverter.java
    bot/QqChannelBot.java
    config/QqProperties.java
    controller/QqWebhookController.java
  src/main/resources/application.yml

rinko-channel-wechat/                              ← WeChat bot (runnable Spring Boot)
  pom.xml
  src/main/java/com/rinko/channel/wechat/
    RinkoChannelWechatApplication.java
    adapter/WechatAdapter.java
    converter/WechatMessageConverter.java
    bot/WechatChannelBot.java
    config/WechatProperties.java
    controller/WechatWebhookController.java
  src/main/resources/application.yml

rinko-channel-dingtalk/                            ← DingTalk bot (runnable Spring Boot)
  pom.xml
  src/main/java/com/rinko/channel/dingtalk/
    RinkoChannelDingtalkApplication.java
    adapter/DingtalkAdapter.java
    converter/DingtalkMessageConverter.java
    bot/DingtalkChannelBot.java
    config/DingtalkProperties.java
    controller/DingtalkWebhookController.java
  src/main/resources/application.yml

rinko-channel-discord/                             ← Discord bot (runnable Spring Boot)
  pom.xml
  src/main/java/com/rinko/channel/discord/
    RinkoChannelDiscordApplication.java
    adapter/DiscordAdapter.java
    converter/DiscordMessageConverter.java
    bot/DiscordChannelBot.java
    config/DiscordProperties.java
    controller/DiscordWebhookController.java
  src/main/resources/application.yml
```

---

### Task 1: Create rinko-channel POM and integrate into root POM

**Files:**
- Create: `rinko-channel/pom.xml`
- Modify: `pom.xml`

- [ ] **Step 1: Create the rinko-channel pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.rinko</groupId>
        <artifactId>rinko-ac</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>rinko-channel</artifactId>
    <packaging>jar</packaging>

    <name>rinko-channel</name>
    <description>Rinko 多平台消息通道公共 API</description>

    <dependencies>
        <dependency>
            <groupId>com.rinko</groupId>
            <artifactId>rinko-infra</artifactId>
        </dependency>
        <dependency>
            <groupId>com.rinko</groupId>
            <artifactId>rinko-ai</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>
        <dependency>
            <groupId>tools.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectreactor</groupId>
            <artifactId>reactor-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Automatic-Module-Name>com.rinko.channel</Automatic-Module-Name>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>repackage</id>
                        <phase>none</phase>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

- [ ] **Step 2: Add rinko-channel to root POM modules list**

Edit `pom.xml` — add `<module>rinko-channel</module>` inside `<modules>`:

```xml
<modules>
    <module>rinko-infra</module>
    <module>rinko-ai</module>
    <module>rinko-channel</module>
    <module>rinko-gateway</module>
    <module>rinko-auth</module>
    <module>rinko-oss</module>
    <module>rinko-log</module>
    <module>rinko-notify</module>
    <module>rinko-scheduler</module>
</modules>
```

- [ ] **Step 3: Verify rinko-channel compiles**

Run: `./mvnw compile -pl rinko-channel -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add rinko-channel/pom.xml pom.xml
git commit -m "feat: add rinko-channel common module scaffold

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 2: Create ChannelEvent sealed hierarchy

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/event/ChannelEvent.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/event/MessageReceivedEvent.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/event/MessageSentEvent.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/event/UserJoinedEvent.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/event/UserLeftEvent.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/event/ChannelLifecycleEvent.java`

- [ ] **Step 1: Write ChannelEvent sealed interface**

```java
package com.rinko.channel.event;

import com.rinko.channel.user.PlatformUserId;

import java.time.Instant;
import java.util.UUID;

public sealed interface ChannelEvent
    permits ChannelEvent.MessageReceivedEvent,
            ChannelEvent.MessageSentEvent,
            ChannelEvent.UserJoinedEvent,
            ChannelEvent.UserLeftEvent,
            ChannelEvent.ChannelLifecycleEvent {

    String eventId();

    Instant timestamp();

    String platformType();

    String botId();

    /** Inbound message from a user on the platform. */
    record MessageReceivedEvent(
        String eventId,
        Instant timestamp,
        String platformType,
        String botId,
        PlatformUserId sender,
        String channelId,
        String messageText,
        String messageId,
        Object nativePayload
    ) implements ChannelEvent {}

    /** Outbound message confirmation (after send succeeds). */
    record MessageSentEvent(
        String eventId,
        Instant timestamp,
        String platformType,
        String botId,
        PlatformUserId recipient,
        String channelId,
        String messageText,
        String platformMessageId
    ) implements ChannelEvent {}

    /** A user joined a group/channel. */
    record UserJoinedEvent(
        String eventId,
        Instant timestamp,
        String platformType,
        String botId,
        PlatformUserId user,
        String channelId
    ) implements ChannelEvent {}

    /** A user left a group/channel. */
    record UserLeftEvent(
        String eventId,
        Instant timestamp,
        String platformType,
        String botId,
        PlatformUserId user,
        String channelId
    ) implements ChannelEvent {}

    /** Bot connection state change. */
    record ChannelLifecycleEvent(
        String eventId,
        Instant timestamp,
        String platformType,
        String botId,
        LifecycleState state,
        String reason
    ) implements ChannelEvent {

        public enum LifecycleState {
            CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, RECONNECTING
        }
    }
}
```

- [ ] **Step 2: Write test that exercises all event subtypes**

```java
package com.rinko.channel.event;

import com.rinko.channel.user.PlatformUserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelEventTest {

    @Test
    void shouldConstructAllEventSubtypes() {
        var sender = new PlatformUserId("DISCORD", "12345");
        var now = Instant.now();

        var msgEvent = new ChannelEvent.MessageReceivedEvent(
            "evt-1", now, "DISCORD", "bot-1",
            sender, "ch-abc", "Hello bot!", "msg-platform-1",
            null
        );

        assertThat(msgEvent.eventId()).isEqualTo("evt-1");
        assertThat(msgEvent.platformType()).isEqualTo("DISCORD");
        assertThat(msgEvent.sender()).isEqualTo(sender);
        assertThat(msgEvent.messageText()).isEqualTo("Hello bot!");

        var lifecycleEvent = new ChannelEvent.ChannelLifecycleEvent(
            "evt-2", now, "QQ", "bot-2",
            ChannelEvent.ChannelLifecycleEvent.LifecycleState.CONNECTED,
            null
        );

        assertThat(lifecycleEvent.state())
            .isEqualTo(ChannelEvent.ChannelLifecycleEvent.LifecycleState.CONNECTED);
    }
}
```

- [ ] **Step 3: Run test to verify**

Run: `./mvnw test -pl rinko-channel -Dtest=ChannelEventTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add rinko-channel/src/
git commit -m "feat: add ChannelEvent sealed hierarchy

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 3: Create PlatformUserId record and UnifiedUser model

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/user/PlatformUserId.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/user/UnifiedUser.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/user/UserMappingService.java`

- [ ] **Step 1: Write PlatformUserId record**

```java
package com.rinko.channel.user;

public record PlatformUserId(
    String platformType,
    String platformUserId
) {}
```

- [ ] **Step 2: Write UnifiedUser entity**

```java
package com.rinko.channel.user;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UnifiedUser {
    private Long id;
    private List<String> linkedIdentities = new ArrayList<>();
    private String defaultPlatformType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: Write UserMappingService interface**

```java
package com.rinko.channel.user;

import java.util.Optional;

public interface UserMappingService {

    /** Find unified user by a platform identity; create if not exists. */
    UnifiedUser resolveUser(PlatformUserId platformId);

    /** Link a new platform identity to an existing unified user. */
    void linkIdentity(Long unifiedUserId, PlatformUserId newPlatformId);

    /** Find by any linked platform identity. */
    Optional<UnifiedUser> findByPlatformUserId(PlatformUserId platformId);
}
```

- [ ] **Step 4: Run compile to verify types align**

Run: `./mvnw compile -pl rinko-channel -am`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add rinko-channel/src/main/java/com/rinko/channel/user/
git commit -m "feat: add PlatformUserId, UnifiedUser, and UserMappingService

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 4: Create Block sealed hierarchy and RichMessage

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/BlockType.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/Block.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/TextBlock.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/ImageBlock.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/ButtonBlock.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/ActionRowBlock.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/EmbedBlock.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/SectionBlock.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/DividerBlock.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/RichMessage.java`

- [ ] **Step 1: Write BlockType enum**

```java
package com.rinko.channel.message;

public enum BlockType {
    TEXT,
    IMAGE,
    BUTTON,
    ACTION_ROW,
    EMBED,
    SECTION,
    DIVIDER
}
```

- [ ] **Step 2: Write sealed Block interface**

```java
package com.rinko.channel.message;

import tools.jackson.annotation.JsonSubTypes;
import tools.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextBlock.class, name = "TEXT"),
    @JsonSubTypes.Type(value = ImageBlock.class, name = "IMAGE"),
    @JsonSubTypes.Type(value = ButtonBlock.class, name = "BUTTON"),
    @JsonSubTypes.Type(value = ActionRowBlock.class, name = "ACTION_ROW"),
    @JsonSubTypes.Type(value = EmbedBlock.class, name = "EMBED"),
    @JsonSubTypes.Type(value = SectionBlock.class, name = "SECTION"),
    @JsonSubTypes.Type(value = DividerBlock.class, name = "DIVIDER")
})
public sealed interface Block
    permits TextBlock, ImageBlock, ButtonBlock, ActionRowBlock,
            EmbedBlock, SectionBlock, DividerBlock {

    BlockType type();
}
```

- [ ] **Step 3: Write concrete block classes**

```java
// TextBlock.java
package com.rinko.channel.message;

public record TextBlock(
    String text,
    TextStyle textStyle
) implements Block {
    @Override public BlockType type() { return BlockType.TEXT; }

    public enum TextStyle { PLAIN, MARKDOWN, BOLD, ITALIC }

    public static TextBlock plain(String text) {
        return new TextBlock(text, TextStyle.PLAIN);
    }

    public static TextBlock markdown(String text) {
        return new TextBlock(text, TextStyle.MARKDOWN);
    }
}

// ImageBlock.java
package com.rinko.channel.message;

public record ImageBlock(
    String imageUrl,
    String altText,
    int width,
    int height
) implements Block {
    @Override public BlockType type() { return BlockType.IMAGE; }

    public ImageBlock(String imageUrl, String altText) {
        this(imageUrl, altText, 0, 0);
    }
}

// ButtonBlock.java
package com.rinko.channel.message;

public record ButtonBlock(
    String text,
    String actionId,
    ButtonStyle style,
    String url
) implements Block {
    @Override public BlockType type() { return BlockType.BUTTON; }

    public enum ButtonStyle { PRIMARY, DANGER, DEFAULT }

    public static ButtonBlock primary(String text, String actionId) {
        return new ButtonBlock(text, actionId, ButtonStyle.PRIMARY, null);
    }

    public static ButtonBlock link(String text, String url) {
        return new ButtonBlock(text, null, ButtonStyle.DEFAULT, url);
    }
}

// ActionRowBlock.java
package com.rinko.channel.message;

import java.util.List;

public record ActionRowBlock(
    List<ButtonBlock> buttons
) implements Block {
    @Override public BlockType type() { return BlockType.ACTION_ROW; }

    public ActionRowBlock {
        buttons = List.copyOf(buttons);
    }
}

// EmbedBlock.java
package com.rinko.channel.message;

import java.util.List;

public record EmbedBlock(
    String title,
    String description,
    String url,
    String color,
    List<EmbedField> fields,
    EmbedThumbnail thumbnail
) implements Block {
    @Override public BlockType type() { return BlockType.EMBED; }

    public record EmbedField(String name, String value, boolean inline) {}

    public record EmbedThumbnail(String url, int width, int height) {}
}

// SectionBlock.java
package com.rinko.channel.message;

public record SectionBlock(
    TextBlock text,
    Block accessory
) implements Block {
    @Override public BlockType type() { return BlockType.SECTION; }
}

// DividerBlock.java
package com.rinko.channel.message;

public record DividerBlock() implements Block {
    @Override public BlockType type() { return BlockType.DIVIDER; }

    public static final DividerBlock INSTANCE = new DividerBlock();
}
```

- [ ] **Step 4: Write RichMessage**

```java
package com.rinko.channel.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record RichMessage(
    String messageId,
    List<Block> blocks,
    String fallbackText,
    Map<String, Object> metadata
) {
    public RichMessage {
        blocks = blocks != null ? List.copyOf(blocks) : List.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RichMessage textOnly(String text) {
        return builder()
            .addBlock(TextBlock.markdown(text))
            .fallbackText(text)
            .build();
    }

    public static class Builder {
        private String messageId;
        private final List<Block> blocks = new ArrayList<>();
        private String fallbackText;
        private Map<String, Object> metadata = Map.of();

        public Builder messageId(String id) { this.messageId = id; return this; }
        public Builder addBlock(Block block) { this.blocks.add(block); return this; }
        public Builder fallbackText(String text) { this.fallbackText = text; return this; }
        public Builder metadata(Map<String, Object> meta) { this.metadata = meta; return this; }

        public RichMessage build() {
            return new RichMessage(messageId, blocks, fallbackText, metadata);
        }
    }
}
```

- [ ] **Step 5: Write test for RichMessage serialization**

```java
package com.rinko.channel.message;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RichMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeAndDeserializeRichMessage() throws Exception {
        var msg = RichMessage.builder()
            .messageId("msg-1")
            .addBlock(TextBlock.plain("Hello"))
            .addBlock(new DividerBlock())
            .addBlock(new ImageBlock("https://example.com/img.png", "alt"))
            .fallbackText("Hello")
            .build();

        String json = objectMapper.writeValueAsString(msg);
        var parsed = objectMapper.readValue(json, RichMessage.class);

        assertThat(parsed.blocks()).hasSize(3);
        assertThat(parsed.blocks().get(0)).isInstanceOf(TextBlock.class);
        assertThat(parsed.blocks().get(1)).isInstanceOf(DividerBlock.class);
        assertThat(parsed.blocks().get(2)).isInstanceOf(ImageBlock.class);
        assertThat(((TextBlock) parsed.blocks().get(0)).text()).isEqualTo("Hello");
    }

    @Test
    void shouldCreateTextOnlyQuickly() {
        var msg = RichMessage.textOnly("Quick reply");
        assertThat(msg.blocks()).hasSize(1);
        assertThat(msg.fallbackText()).isEqualTo("Quick reply");
    }
}
```

- [ ] **Step 6: Run test to verify**

Run: `./mvnw test -pl rinko-channel -Dtest=RichMessageTest`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add rinko-channel/src/main/java/com/rinko/channel/message/ rinko-channel/src/test/
git commit -m "feat: add Block hierarchy and RichMessage model

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 5: Create MessageConverter interface

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/message/MessageConverter.java`

- [ ] **Step 1: Write MessageConverter interface**

```java
package com.rinko.channel.message;

public interface MessageConverter<T> {

    /** Platform-native type this converter handles. */
    Class<T> getNativeType();

    /** Convert a platform-native message to RichMessage. */
    RichMessage toRichMessage(T nativeMessage);

    /** Convert a RichMessage to a platform-native payload. */
    T toNativeMessage(RichMessage richMessage);

    /** Whether this platform supports a given Block type natively. */
    default boolean supportsBlock(BlockType type) {
        return true; // Default optimistic; subclasses override
    }
}
```

- [ ] **Step 2: Verify compile and commit**

Run: `./mvnw compile -pl rinko-channel -am` → BUILD SUCCESS

```bash
git add rinko-channel/src/main/java/com/rinko/channel/message/MessageConverter.java
git commit -m "feat: add MessageConverter interface

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 6: Create ChannelBot interface, BotContext, and AbstractChannelBot

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/bot/BotContext.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/bot/ChannelBot.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/bot/AbstractChannelBot.java`
- Create: `rinko-channel/src/test/java/com/rinko/channel/bot/AbstractChannelBotTest.java`

- [ ] **Step 1: Write BotContext record**

```java
package com.rinko.channel.bot;

import java.util.Map;

public record BotContext(
    String platformType,
    String botId,
    Map<String, Object> config
) {}
```

- [ ] **Step 2: Write ChannelBot interface**

```java
package com.rinko.channel.bot;

import com.rinko.channel.event.ChannelEvent;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.user.PlatformUserId;

import java.util.concurrent.CompletableFuture;

public interface ChannelBot {

    String getPlatform();

    void onEvent(ChannelEvent event);

    CompletableFuture<String> send(PlatformUserId recipient, RichMessage message, String channelId);

    void start(BotContext context);

    void stop();

    ChannelStatus getStatus();

    record ChannelStatus(
        String platform,
        String botId,
        State state,
        String detail
    ) {
        public enum State { INIT, CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED }
    }
}
```

- [ ] **Step 3: Write AbstractChannelBot**

```java
package com.rinko.channel.bot;

import com.rinko.channel.event.ChannelEvent;
import com.rinko.channel.event.ChannelEvent.MessageReceivedEvent;
import com.rinko.channel.event.ChannelEvent.ChannelLifecycleEvent;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.user.PlatformUserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractChannelBot implements ChannelBot {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final AtomicReference<ChannelStatus.State> state =
        new AtomicReference<>(ChannelStatus.State.INIT);
    private BotContext context;

    @Override
    public final void onEvent(ChannelEvent event) {
        try {
            switch (event) {
                case MessageReceivedEvent msg -> onMessageReceived(msg);
                case ChannelEvent.MessageSentEvent sent -> onMessageSent(sent);
                case ChannelEvent.UserJoinedEvent joined -> onUserJoined(joined);
                case ChannelEvent.UserLeftEvent left -> onUserLeft(left);
                case ChannelLifecycleEvent lifecycle -> onLifecycle(lifecycle);
            }
        } catch (Exception e) {
            log.error("Error handling event {}: {}", event.eventId(), e.getMessage(), e);
        }
    }

    protected void onMessageReceived(MessageReceivedEvent event) {
        log.info("[{}] Message from {}: {}", getPlatform(), event.sender(), event.messageText());
    }

    protected void onMessageSent(ChannelEvent.MessageSentEvent event) {
        log.debug("[{}] Message sent to {}: {}", getPlatform(), event.recipient(), event.messageText());
    }

    protected void onUserJoined(ChannelEvent.UserJoinedEvent event) {
        log.info("[{}] User joined: {}", getPlatform(), event.user());
    }

    protected void onUserLeft(ChannelEvent.UserLeftEvent event) {
        log.info("[{}] User left: {}", getPlatform(), event.user());
    }

    protected void onLifecycle(ChannelLifecycleEvent event) {
        log.info("[{}] Lifecycle: {}", getPlatform(), event.state());
    }

    @Override
    public abstract CompletableFuture<String> send(
        PlatformUserId recipient, RichMessage message, String channelId);

    @Override
    public void start(BotContext context) {
        this.context = context;
        setState(ChannelStatus.State.CONNECTING);
        doStart(context);
        setState(ChannelStatus.State.CONNECTED);
        log.info("[{}] Bot {} started", getPlatform(), context.botId());
    }

    protected abstract void doStart(BotContext context);

    @Override
    public void stop() {
        setState(ChannelStatus.State.DISCONNECTING);
        doStop();
        setState(ChannelStatus.State.DISCONNECTED);
        log.info("[{}] Bot stopped", getPlatform());
    }

    protected abstract void doStop();

    @Override
    public ChannelStatus getStatus() {
        return new ChannelStatus(
            getPlatform(),
            context != null ? context.botId() : null,
            state.get(),
            null
        );
    }

    protected void setState(ChannelStatus.State newState) {
        state.set(newState);
    }

    protected ChannelStatus.State getState() {
        return state.get();
    }

    protected BotContext context() {
        return context;
    }

    protected ChannelLifecycleEvent createLifecycleEvent(
        ChannelLifecycleEvent.LifecycleState s, String reason) {
        return new ChannelLifecycleEvent(
            UUID.randomUUID().toString(), Instant.now(), getPlatform(),
            context != null ? context.botId() : null, s, reason);
    }
}
```

- [ ] **Step 4: Write test for AbstractChannelBot lifecycle**

```java
package com.rinko.channel.bot;

import com.rinko.channel.event.ChannelEvent;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.user.PlatformUserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractChannelBotTest {

    private final TestBot bot = new TestBot();

    @Test
    void shouldTransitionThroughLifecycleStates() {
        var ctx = new BotContext("TEST", "bot-1", Map.of());
        bot.start(ctx);

        assertThat(bot.getStatus().state())
            .isEqualTo(ChannelBot.ChannelStatus.State.CONNECTED);
        assertThat(bot.getPlatform()).isEqualTo("TEST");

        bot.stop();
        assertThat(bot.getStatus().state())
            .isEqualTo(ChannelBot.ChannelStatus.State.DISCONNECTED);
    }

    @Test
    void shouldDispatchMessageEvents() {
        bot.start(new BotContext("TEST", "bot-1", Map.of()));

        var event = new ChannelEvent.MessageReceivedEvent(
            "evt-1", Instant.now(), "TEST", "bot-1",
            new PlatformUserId("TEST", "user123"), "ch-1",
            "Hello", "native-1", null
        );

        bot.onEvent(event);
        assertThat(bot.getLastReceivedMessage()).isEqualTo("Hello");
    }

    static class TestBot extends AbstractChannelBot {
        private String lastReceivedMessage;

        @Override
        public String getPlatform() { return "TEST"; }

        @Override
        protected void onMessageReceived(MessageReceivedEvent event) {
            this.lastReceivedMessage = event.messageText();
        }

        @Override
        public CompletableFuture<String> send(
            PlatformUserId recipient, RichMessage message, String channelId) {
            return CompletableFuture.completedFuture("ok");
        }

        @Override
        protected void doStart(BotContext context) {
            // no-op for test
        }

        @Override
        protected void doStop() {
            // no-op for test
        }

        public String getLastReceivedMessage() { return lastReceivedMessage; }
    }
}
```

- [ ] **Step 5: Run test**

Run: `./mvnw test -pl rinko-channel -Dtest=AbstractChannelBotTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add rinko-channel/src/main/java/com/rinko/channel/bot/ rinko-channel/src/test/
git commit -m "feat: add ChannelBot interface and AbstractChannelBot

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 7: Create AiBridge and implementations

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/ai/AiBridge.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/ai/LocalAiBridge.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/ai/RemoteAiBridge.java`

- [ ] **Step 1: Write AiBridge interface**

```java
package com.rinko.channel.ai;

import com.rinko.channel.message.RichMessage;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import com.rinko.channel.persistence.entity.ConversationSummary;
import com.rinko.channel.user.UnifiedUser;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiBridge {

    CompletableFuture<RichMessage> generateReply(
        UnifiedUser user,
        RichMessage currentMessage,
        List<ChannelMessageHistory> recentHistory,
        List<ConversationSummary> summaries
    );

    boolean isAvailable();
}
```

- [ ] **Step 2: Write LocalAiBridge**

```java
package com.rinko.channel.ai;

import com.rinko.ai.agent.ChatAgentService;
import com.rinko.ai.model.ChatRequest;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import com.rinko.channel.persistence.entity.ConversationSummary;
import com.rinko.channel.user.UnifiedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@ConditionalOnBean(ChatAgentService.class)
public class LocalAiBridge implements AiBridge {

    private static final Logger log = LoggerFactory.getLogger(LocalAiBridge.class);
    private final ChatAgentService chatAgentService;

    public LocalAiBridge(ChatAgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    @Override
    public CompletableFuture<RichMessage> generateReply(
        UnifiedUser user,
        RichMessage currentMessage,
        List<ChannelMessageHistory> recentHistory,
        List<ConversationSummary> summaries
    ) {
        String prompt = buildPrompt(currentMessage, recentHistory, summaries);
        // We use the fallback text as the plain text for the AI; the AI returns text
        String userMessage = currentMessage.fallbackText();
        if (userMessage == null && !currentMessage.blocks().isEmpty()) {
            userMessage = currentMessage.blocks().stream()
                .filter(b -> b instanceof com.rinko.channel.message.TextBlock)
                .map(b -> ((com.rinko.channel.message.TextBlock) b).text())
                .collect(Collectors.joining("\n"));
        }
        if (userMessage == null || userMessage.isBlank()) {
            userMessage = "Hello";
        }

        ChatRequest request = new ChatRequest();
        request.setMessage(userMessage);
        request.setSessionId("channel-" + user.getId());

        return CompletableFuture.supplyAsync(() -> {
            try {
                var response = chatAgentService.chat(request);
                return RichMessage.textOnly(
                    response.getContent() != null ? response.getContent() : "I'm not sure how to respond.");
            } catch (Exception e) {
                log.error("AI generation failed for user {}", user.getId(), e);
                return RichMessage.textOnly("Sorry, I'm having trouble thinking right now.");
            }
        });
    }

    private String buildPrompt(
        RichMessage current,
        List<ChannelMessageHistory> history,
        List<ConversationSummary> summaries
    ) {
        var sb = new StringBuilder();
        if (!summaries.isEmpty()) {
            sb.append("Previous conversation summaries:\n");
            for (var s : summaries) {
                sb.append("- ").append(s.getSummaryText()).append("\n");
            }
            sb.append("---\n");
        }
        return sb.toString();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
```

- [ ] **Step 3: Write RemoteAiBridge**

```java
package com.rinko.channel.ai;

import com.rinko.channel.config.ChannelProperties;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import com.rinko.channel.persistence.entity.ConversationSummary;
import com.rinko.channel.user.UnifiedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnMissingBean(name = "localAiBridge")
public class RemoteAiBridge implements AiBridge {

    private static final Logger log = LoggerFactory.getLogger(RemoteAiBridge.class);
    private final RestTemplate restTemplate;
    private final ChannelProperties properties;

    public RemoteAiBridge(ChannelProperties properties, RestTemplateBuilder builder) {
        this.properties = properties;
        this.restTemplate = builder
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(30))
            .build();
    }

    @Override
    public CompletableFuture<RichMessage> generateReply(
        UnifiedUser user,
        RichMessage currentMessage,
        List<ChannelMessageHistory> recentHistory,
        List<ConversationSummary> summaries
    ) {
        String aiUrl = properties.getAi().getRemoteUrl();

        return CompletableFuture.supplyAsync(() -> {
            try {
                var body = Map.of(
                    "message", currentMessage.fallbackText() != null
                        ? currentMessage.fallbackText() : "",
                    "sessionId", "channel-" + user.getId()
                );
                @SuppressWarnings("unchecked")
                var response = restTemplate.postForObject(
                    aiUrl + "/api/ai/chat", body, Map.class);
                if (response != null && response.containsKey("content")) {
                    return RichMessage.textOnly((String) response.get("content"));
                }
                return RichMessage.textOnly("I'm not sure how to respond.");
            } catch (Exception e) {
                log.error("Remote AI call failed for user {}", user.getId(), e);
                return RichMessage.textOnly("Sorry, I'm having trouble right now.");
            }
        });
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForObject(properties.getAi().getRemoteUrl() + "/api/ai/health", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

- [ ] **Step 4: Verify compile**

Run: `./mvnw compile -pl rinko-channel -am`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add rinko-channel/src/main/java/com/rinko/channel/ai/
git commit -m "feat: add AiBridge with LocalAiBridge and RemoteAiBridge

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 8: Create ChannelProperties and ChannelAutoConfiguration

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/config/ChannelProperties.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/config/ChannelAutoConfiguration.java`

- [ ] **Step 1: Write ChannelProperties**

```java
package com.rinko.channel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel")
public class ChannelProperties {

    private Ai ai = new Ai();
    private Persistence persistence = new Persistence();

    @Getter
    @Setter
    public static class Ai {
        private String remoteUrl = "http://localhost:8083";
    }

    @Getter
    @Setter
    public static class Persistence {
        private Compression compression = new Compression();

        @Getter
        @Setter
        public static class Compression {
            private boolean enabled = true;
            private String hotWindow = "1h";
            private String warmWindow = "7d";
            private Retention retention = new Retention();

            @Getter
            @Setter
            public static class Retention {
                private int rawMessagesDays = 90;
                private boolean summaryForever = true;
            }
        }
    }
}
```

- [ ] **Step 2: Write ChannelAutoConfiguration**

```java
package com.rinko.channel.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@AutoConfiguration
public class ChannelAutoConfiguration {

    @Bean
    public Cache<String, ChannelMessageHistory> messageHotCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(1))
            .build();
    }
}
```

- [ ] **Step 3: Create spring.factories for auto-configuration**

Create directory `rinko-channel/src/main/resources/META-INF/` and file `org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```
com.rinko.channel.config.ChannelAutoConfiguration
```

- [ ] **Step 4: Verify compile**

Run: `./mvnw compile -pl rinko-channel -am`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add rinko-channel/src/main/java/com/rinko/channel/config/ rinko-channel/src/main/resources/
git commit -m "feat: add ChannelProperties and ChannelAutoConfiguration

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 9: Create persistence entities and MyBatis-Plus mappers

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/persistence/entity/ChannelMessageHistory.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/persistence/entity/ConversationSummary.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/persistence/ChannelMessageHistoryMapper.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/persistence/ConversationSummaryMapper.java`

- [ ] **Step 1: Write ChannelMessageHistory entity**

```java
package com.rinko.channel.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("channel_message_history")
public class ChannelMessageHistory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String platformType;
    private String senderId;
    private String recipientId;
    private String channelId;
    private String direction;      // INBOUND or OUTBOUND
    private String messageText;
    private String richMessageJson;
    private String status;         // PENDING, SENT, FAILED
    private Boolean compressed;
    private Long summaryId;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: Write ConversationSummary entity**

```java
package com.rinko.channel.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("channel_conversation_summary")
public class ConversationSummary {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long unifiedUserId;
    private String platformType;
    private String summaryText;
    private Integer originalMessageCount;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private LocalDateTime compressedAt;
}
```

- [ ] **Step 3: Write ChannelMessageHistoryMapper**

```java
package com.rinko.channel.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChannelMessageHistoryMapper extends BaseMapper<ChannelMessageHistory> {
}
```

- [ ] **Step 4: Write ConversationSummaryMapper**

```java
package com.rinko.channel.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.channel.persistence.entity.ConversationSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationSummaryMapper extends BaseMapper<ConversationSummary> {
}
```

- [ ] **Step 5: Verify compile**

Run: `./mvnw compile -pl rinko-channel -am`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add rinko-channel/src/main/java/com/rinko/channel/persistence/
git commit -m "feat: add persistence entities and MyBatis-Plus mappers

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 10: Create MessagePersistenceService

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/persistence/MessagePersistenceService.java`

- [ ] **Step 1: Write MessagePersistenceService**

```java
package com.rinko.channel.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessagePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(MessagePersistenceService.class);

    private final ChannelMessageHistoryMapper historyMapper;
    private final Cache<String, ChannelMessageHistory> hotCache;

    public MessagePersistenceService(
        ChannelMessageHistoryMapper historyMapper,
        Cache<String, ChannelMessageHistory> hotCache
    ) {
        this.historyMapper = historyMapper;
        this.hotCache = hotCache;
    }

    /** Persist an inbound or outbound message. */
    public void save(ChannelMessageHistory history) {
        history.setCreatedAt(LocalDateTime.now());
        historyMapper.insert(history);
        hotCache.put("msg:" + history.getId(), history);
    }

    /** Get recent messages for a user within a time window (warm tier). */
    public List<ChannelMessageHistory> getRecentMessages(
        String platformType, String senderId, LocalDateTime since) {
        return historyMapper.selectList(new LambdaQueryWrapper<ChannelMessageHistory>()
            .eq(ChannelMessageHistory::getPlatformType, platformType)
            .eq(ChannelMessageHistory::getSenderId, senderId)
            .ge(ChannelMessageHistory::getCreatedAt, since)
            .orderByAsc(ChannelMessageHistory::getCreatedAt));
    }

    /** Find messages older than threshold that have not been compressed. */
    public List<ChannelMessageHistory> findUncompressedMessages(
        LocalDateTime before, int limit) {
        return historyMapper.selectList(new LambdaQueryWrapper<ChannelMessageHistory>()
            .lt(ChannelMessageHistory::getCreatedAt, before)
            .ne(ChannelMessageHistory::getCompressed, true)
            .last("LIMIT " + limit));
    }

    /** Mark a batch of messages as compressed. */
    public void markCompressed(List<Long> messageIds, Long summaryId) {
        for (var id : messageIds) {
            var msg = new ChannelMessageHistory();
            msg.setId(id);
            msg.setCompressed(true);
            msg.setSummaryId(summaryId);
            historyMapper.updateById(msg);
        }
    }

    /** Delete raw messages older than retention days. */
    public int deleteOlderThan(LocalDateTime cutoff) {
        var count = historyMapper.delete(new LambdaQueryWrapper<ChannelMessageHistory>()
            .lt(ChannelMessageHistory::getCreatedAt, cutoff));
        log.info("Deleted {} raw messages older than {}", count, cutoff);
        return count;
    }
}
```

- [ ] **Step 2: Verify compile**

Run: `./mvnw compile -pl rinko-channel -am`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add rinko-channel/src/main/java/com/rinko/channel/persistence/MessagePersistenceService.java
git commit -m "feat: add MessagePersistenceService with hot/warm/cold routing

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 11: Create MessageCompressionTask (bean for rinko-scheduler)

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/persistence/MessageCompressionTask.java`
- Create: `rinko-channel/src/test/java/com/rinko/channel/persistence/MessageCompressionTaskTest.java`

- [ ] **Step 1: Write test for MessageCompressionTask**

```java
package com.rinko.channel.persistence;

import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class MessageCompressionTaskTest {

    @Test
    void shouldCompressMessagesOlderThanWarmWindow() {
        var historyMapper = mock(ChannelMessageHistoryMapper.class);
        var summaryMapper = mock(ConversationSummaryMapper.class);
        var persistenceService = new MessagePersistenceService(historyMapper, null);

        var oldMsg = new ChannelMessageHistory();
        oldMsg.setId(1L);
        oldMsg.setPlatformType("DISCORD");
        oldMsg.setSenderId("user-1");
        oldMsg.setMessageText("Hello old message");
        oldMsg.setCompressed(false);
        oldMsg.setCreatedAt(LocalDateTime.now().minusDays(10));

        when(historyMapper.selectList(any()))
            .thenReturn(List.of(oldMsg))
            .thenReturn(List.of()); // second call returns empty

        var task = new MessageCompressionTask(
            persistenceService,
            summaryMapper,
            null // no AI bridge in unit test
        );

        String result = task.compress();
        assertThat(result).contains("Compressed");
        verify(historyMapper, atLeastOnce()).updateById(any());
        verify(summaryMapper, atLeastOnce()).insert(any());
    }
}
```

- [ ] **Step 2: Run test to verify it fails (no implementation yet)**

Run: `./mvnw test -pl rinko-channel -Dtest=MessageCompressionTaskTest`
Expected: FAIL (compilation error — class not found)

- [ ] **Step 3: Write MessageCompressionTask**

```java
package com.rinko.channel.persistence;

import com.rinko.channel.ai.AiBridge;
import com.rinko.channel.persistence.entity.ConversationSummary;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("messageCompressionTask")
public class MessageCompressionTask {

    private static final Logger log = LoggerFactory.getLogger(MessageCompressionTask.class);

    private final MessagePersistenceService persistenceService;
    private final ConversationSummaryMapper summaryMapper;
    @Autowired(required = false)
    private AiBridge aiBridge;

    public MessageCompressionTask(
        MessagePersistenceService persistenceService,
        ConversationSummaryMapper summaryMapper
    ) {
        this.persistenceService = persistenceService;
        this.summaryMapper = summaryMapper;
    }

    /**
     * Entry point for rinko-scheduler BeanJobExecutor.
     * Invoked via reflection: beanName=messageCompressionTask, methodName=compress
     */
    public String compress() {
        if (aiBridge == null || !aiBridge.isAvailable()) {
            log.warn("Compression skipped: AiBridge not available");
            return "SKIPPED: AiBridge not available";
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<ChannelMessageHistory> messages = persistenceService.findUncompressedMessages(cutoff, 500);

        if (messages.isEmpty()) {
            log.info("No messages to compress (cutoff: {})", cutoff);
            return "OK: No messages to compress";
        }

        // Group by sender
        Map<String, List<ChannelMessageHistory>> grouped = messages.stream()
            .collect(Collectors.groupingBy(m ->
                m.getPlatformType() + ":" + m.getSenderId()));

        int groupsCompressed = 0;
        int totalMessages = 0;

        for (var entry : grouped.entrySet()) {
            var batch = entry.getValue();
            if (batch.isEmpty()) continue;

            try {
                // Generate summary text
                String summaryText = generateSimpleSummary(batch);
                var rangeStart = batch.get(0).getCreatedAt();
                var rangeEnd = batch.get(batch.size() - 1).getCreatedAt();
                var ids = batch.stream().map(ChannelMessageHistory::getId).toList();

                // Save summary
                var summary = new ConversationSummary();
                summary.setSummaryText(summaryText);
                summary.setOriginalMessageCount(batch.size());
                summary.setRangeStart(rangeStart);
                summary.setRangeEnd(rangeEnd);
                summary.setCompressedAt(LocalDateTime.now());
                summary.setPlatformType(batch.get(0).getPlatformType());
                summaryMapper.insert(summary);

                // Mark originals as compressed
                persistenceService.markCompressed(ids, summary.getId());

                groupsCompressed++;
                totalMessages += batch.size();
            } catch (Exception e) {
                log.error("Failed to compress batch for {}", entry.getKey(), e);
            }
        }

        String result = String.format(
            "Compressed %d groups, %d messages, cutoff=%s",
            groupsCompressed, totalMessages, cutoff
        );
        log.info(result);
        return result;
    }

    /**
     * Generate a simple summary without calling AI for each batch.
     * For AI-powered summarization, override or enhance with AiBridge.
     */
    private String generateSimpleSummary(List<ChannelMessageHistory> messages) {
        var texts = messages.stream()
            .map(ChannelMessageHistory::getMessageText)
            .filter(t -> t != null && !t.isBlank())
            .limit(50)
            .toList();
        return "Conversation with " + texts.size() + " messages: "
            + String.join(" | ", texts.stream().limit(5).toList());
    }
}
```

- [ ] **Step 4: Run test to verify**

Run: `./mvnw test -pl rinko-channel -Dtest=MessageCompressionTaskTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add rinko-channel/src/main/java/com/rinko/channel/persistence/MessageCompressionTask.java
git add rinko-channel/src/test/java/com/rinko/channel/persistence/MessageCompressionTaskTest.java
git commit -m "feat: add MessageCompressionTask bean for rinko-scheduler

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 12: Create ChannelManager, health indicator, and REST controller

**Files:**
- Create: `rinko-channel/src/main/java/com/rinko/channel/management/ChannelManager.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/management/ChannelHealthIndicator.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/management/controller/ChannelManageController.java`
- Create: `rinko-channel/src/main/java/com/rinko/channel/model/vo/ChannelStatusVO.java`

- [ ] **Step 1: Write ChannelStatusVO**

```java
package com.rinko.channel.model.vo;

public record ChannelStatusVO(
    String platform,
    String botId,
    String state,
    String detail,
    long messageCount
) {}
```

- [ ] **Step 2: Write ChannelManager**

```java
package com.rinko.channel.management;

import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.model.vo.ChannelStatusVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelManager {

    private static final Logger log = LoggerFactory.getLogger(ChannelManager.class);

    private final Map<String, ChannelBot> bots = new ConcurrentHashMap<>();

    public void register(String botId, ChannelBot bot) {
        bots.put(botId, bot);
        log.info("Registered bot: {} ({})", botId, bot.getPlatform());
    }

    public void unregister(String botId) {
        var bot = bots.remove(botId);
        if (bot != null) {
            bot.stop();
        }
    }

    public void startBot(String botId) {
        var bot = bots.get(botId);
        if (bot != null) {
            bot.start(bot.context() != null ? bot.context() : null);
        }
    }

    public void stopBot(String botId) {
        var bot = bots.get(botId);
        if (bot != null) {
            bot.stop();
        }
    }

    public List<ChannelStatusVO> listBots() {
        return bots.values().stream()
            .map(b -> {
                var s = b.getStatus();
                return new ChannelStatusVO(
                    s.platform(), s.botId(), s.state().name(), s.detail(), 0);
            })
            .toList();
    }

    public ChannelStatusVO getBotStatus(String botId) {
        var bot = bots.get(botId);
        if (bot == null) return null;
        var s = bot.getStatus();
        return new ChannelStatusVO(
            s.platform(), s.botId(), s.state().name(), s.detail(), 0);
    }
}
```

- [ ] **Step 3: Write ChannelHealthIndicator**

```java
package com.rinko.channel.management;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class ChannelHealthIndicator implements HealthIndicator {

    private final ChannelManager channelManager;

    public ChannelHealthIndicator(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public Health health() {
        var bots = channelManager.listBots();
        if (bots.isEmpty()) {
            return Health.down().withDetail("reason", "No bots registered").build();
        }
        long connected = bots.stream()
            .filter(b -> "CONNECTED".equals(b.state())).count();
        return Health.up()
            .withDetail("total", bots.size())
            .withDetail("connected", connected)
            .build();
    }
}
```

- [ ] **Step 4: Write ChannelManageController**

```java
package com.rinko.channel.management.controller;

import com.rinko.channel.management.ChannelManager;
import com.rinko.channel.model.vo.ChannelStatusVO;
import com.rinko.infra.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channel")
public class ChannelManageController {

    private final ChannelManager channelManager;

    public ChannelManageController(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @GetMapping("/bots")
    public ApiResponse<List<ChannelStatusVO>> listBots() {
        return ApiResponse.success(channelManager.listBots());
    }

    @PostMapping("/bots/{botId}/start")
    public ApiResponse<Void> startBot(@PathVariable String botId) {
        channelManager.startBot(botId);
        return ApiResponse.success();
    }

    @PostMapping("/bots/{botId}/stop")
    public ApiResponse<Void> stopBot(@PathVariable String botId) {
        channelManager.stopBot(botId);
        return ApiResponse.success();
    }

    @GetMapping("/bots/{botId}/status")
    public ApiResponse<ChannelStatusVO> getBotStatus(@PathVariable String botId) {
        var status = channelManager.getBotStatus(botId);
        if (status == null) {
            return new ApiResponse<>(404, "Bot not found", null, java.time.LocalDateTime.now());
        }
        return ApiResponse.success(status);
    }
}
```

- [ ] **Step 5: Verify compile**

Run: `./mvnw compile -pl rinko-channel -am`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add rinko-channel/src/main/java/com/rinko/channel/management/
git add rinko-channel/src/main/java/com/rinko/channel/model/
git commit -m "feat: add ChannelManager, health indicator, and REST controller

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 13: Create rinko-channel-qq module scaffold

**Files:**
- Create: `rinko-channel-qq/pom.xml`
- Create: `rinko-channel-qq/src/main/java/com/rinko/channel/qq/RinkoChannelQqApplication.java`
- Create: `rinko-channel-qq/src/main/java/com/rinko/channel/qq/config/QqProperties.java`
- Create: `rinko-channel-qq/src/main/java/com/rinko/channel/qq/adapter/QqAdapter.java`
- Create: `rinko-channel-qq/src/main/java/com/rinko/channel/qq/bot/QqChannelBot.java`
- Create: `rinko-channel-qq/src/main/java/com/rinko/channel/qq/converter/QqMessageConverter.java`
- Create: `rinko-channel-qq/src/main/resources/application.yml`
- Modify: `pom.xml`

- [ ] **Step 1: Create rinko-channel-qq/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.rinko</groupId>
        <artifactId>rinko-ac</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>rinko-channel-qq</artifactId>
    <packaging>jar</packaging>

    <name>rinko-channel-qq</name>
    <description>Rinko QQ 机器人通道</description>

    <dependencies>
        <dependency>
            <groupId>com.rinko</groupId>
            <artifactId>rinko-channel</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Automatic-Module-Name>com.rinko.channel.qq</Automatic-Module-Name>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

- [ ] **Step 2: Add rinko-channel-qq to root POM modules**

Edit `pom.xml` — add `<module>rinko-channel-qq</module>` right after `rinko-channel`.

- [ ] **Step 3: Write QqProperties**

```java
package com.rinko.channel.qq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel.qq")
public class QqProperties {

    private boolean enabled = false;
    private String botId;
    private String clientSecret;
    private String apiBaseUrl = "https://api.sgroup.qq.com";
}
```

- [ ] **Step 4: Write QQ Application entry point**

```java
package com.rinko.channel.qq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
public class RinkoChannelQqApplication {

    public static void main(String[] args) {
        SpringApplication.run(RinkoChannelQqApplication.class, args);
    }
}
```

- [ ] **Step 5: Write QqAdapter (stub implementation)**

```java
package com.rinko.channel.qq.adapter;

import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.qq.config.QqProperties;
import com.rinko.channel.management.ChannelManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rinko.channel.qq.enabled", havingValue = "true")
public class QqAdapter {

    private static final Logger log = LoggerFactory.getLogger(QqAdapter.class);
    private final QqProperties properties;
    private final ChannelBot qqBot;
    private final ChannelManager channelManager;

    public QqAdapter(QqProperties properties, ChannelBot qqBot, ChannelManager channelManager) {
        this.properties = properties;
        this.qqBot = qqBot;
        this.channelManager = channelManager;
    }

    @PostConstruct
    public void connect() {
        log.info("QQ adapter initializing for bot {}", properties.getBotId());
        // TODO: Initialize QQ WebSocket connection using QQ Bot SDK
        // For now, register the bot
        channelManager.register(properties.getBotId(), qqBot);
        log.info("QQ adapter registered bot {}", properties.getBotId());
    }

    @PreDestroy
    public void disconnect() {
        channelManager.unregister(properties.getBotId());
        log.info("QQ adapter disconnected");
    }
}
```

- [ ] **Step 6: Write QqChannelBot**

```java
package com.rinko.channel.qq.bot;

import com.rinko.channel.bot.AbstractChannelBot;
import com.rinko.channel.bot.BotContext;
import com.rinko.channel.event.ChannelEvent;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.user.PlatformUserId;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class QqChannelBot extends AbstractChannelBot {

    @Override
    public String getPlatform() { return "QQ"; }

    @Override
    protected void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);
        // TODO: Process via AiBridge, generate reply, send back
    }

    @Override
    public CompletableFuture<String> send(
        PlatformUserId recipient, RichMessage message, String channelId) {
        // TODO: Call QQ REST API to send message
        log.info("[QQ] Sending message to {} in {}: {}",
            recipient.platformUserId(), channelId, message.fallbackText());
        return CompletableFuture.completedFuture("stub-msg-id-" + System.currentTimeMillis());
    }

    @Override
    protected void doStart(BotContext context) {
        log.info("[QQ] Bot starting with context: {}", context);
    }

    @Override
    protected void doStop() {
        log.info("[QQ] Bot stopping");
    }
}
```

- [ ] **Step 7: Write QqMessageConverter (stub)**

```java
package com.rinko.channel.qq.converter;

import com.rinko.channel.message.BlockType;
import com.rinko.channel.message.MessageConverter;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.message.TextBlock;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QqMessageConverter implements MessageConverter<Map<String, Object>> {

    @Override
    public Class<Map<String, Object>> getNativeType() {
        @SuppressWarnings("unchecked")
        var type = (Class<Map<String, Object>>) (Class<?>) Map.class;
        return type;
    }

    /** Convert QQ native message payload to RichMessage. */
    @Override
    public RichMessage toRichMessage(Map<String, Object> nativeMsg) {
        String content = (String) nativeMsg.getOrDefault("content", "");
        String msgId = (String) nativeMsg.getOrDefault("id", "unknown");
        return RichMessage.builder()
            .messageId(msgId)
            .addBlock(TextBlock.markdown(content))
            .fallbackText(content)
            .build();
    }

    /** Convert RichMessage to QQ native send payload. */
    @Override
    public Map<String, Object> toNativeMessage(RichMessage richMessage) {
        String text = richMessage.fallbackText();
        if (text == null) {
            text = richMessage.blocks().stream()
                .filter(b -> b instanceof TextBlock)
                .map(b -> ((TextBlock) b).text())
                .findFirst()
                .orElse("");
        }
        return Map.of(
            "content", text,
            "msg_type", 0,
            "msg_id", richMessage.messageId() != null ? richMessage.messageId() : ""
        );
    }

    @Override
    public boolean supportsBlock(BlockType type) {
        return switch (type) {
            case TEXT, IMAGE, DIVIDER -> true;
            case BUTTON, ACTION_ROW -> false; // QQ doesn't natively support interactive buttons in all contexts
            case EMBED, SECTION -> true; // QQ supports rich card-like embeds
        };
    }
}
```

- [ ] **Step 8: Write application.yml**

```yaml
spring:
  application:
    name: rinko-channel-qq
  config:
    import:
      - optional:nacos:application-dev.yml
      - optional:nacos:rinko-channel-qq-dev.yml
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER:127.0.0.1:18848}
        namespace: ${NACOS_NAMESPACE:rinko}
        group: anchorage
        username: nacos
        password: isincere.
      config:
        server-addr: ${NACOS_SERVER:127.0.0.1:18848}
        namespace: ${NACOS_NAMESPACE:rinko}
        group: anchorage
        username: nacos
        password: isincere.
        file-extension: yml
server:
  port: 8090

rinko:
  channel:
    qq:
      enabled: false
      bot-id: ${QQ_BOT_ID:}
      client-secret: ${QQ_CLIENT_SECRET:}
```

- [ ] **Step 9: Verify compile**

Run: `./mvnw compile -pl rinko-channel-qq -am`
Expected: BUILD SUCCESS

- [ ] **Step 10: Commit**

```bash
git add rinko-channel-qq/ pom.xml
git commit -m "feat: add rinko-channel-qq module scaffold

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 14: Create rinko-channel-wechat module scaffold

**Files:**
- Create: `rinko-channel-wechat/pom.xml`
- Create: `rinko-channel-wechat/src/main/java/com/rinko/channel/wechat/RinkoChannelWechatApplication.java`
- Create: `rinko-channel-wechat/src/main/java/com/rinko/channel/wechat/config/WechatProperties.java`
- Create: `rinko-channel-wechat/src/main/java/com/rinko/channel/wechat/adapter/WechatAdapter.java`
- Create: `rinko-channel-wechat/src/main/java/com/rinko/channel/wechat/bot/WechatChannelBot.java`
- Create: `rinko-channel-wechat/src/main/java/com/rinko/channel/wechat/converter/WechatMessageConverter.java`
- Create: `rinko-channel-wechat/src/main/resources/application.yml`
- Modify: `pom.xml`

- [ ] **Step 1: Create pom.xml and directory structure**

Create `rinko-channel-wechat/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.rinko</groupId>
        <artifactId>rinko-ac</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>rinko-channel-wechat</artifactId>
    <packaging>jar</packaging>

    <name>rinko-channel-wechat</name>
    <description>Rinko 微信机器人通道</description>

    <dependencies>
        <dependency>
            <groupId>com.rinko</groupId>
            <artifactId>rinko-channel</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Automatic-Module-Name>com.rinko.channel.wechat</Automatic-Module-Name>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

- [ ] **Step 2: Add module to root POM**

Edit `pom.xml` — add `<module>rinko-channel-wechat</module>` after `rinko-channel-qq`.

- [ ] **Step 3: Write Java classes**

**WechatProperties.java:**
```java
package com.rinko.channel.wechat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel.wechat")
public class WechatProperties {
    private boolean enabled = false;
    private String appId;
    private String appSecret;
    private String token;
    private String encodingAesKey;
}
```

**RinkoChannelWechatApplication.java:**
```java
package com.rinko.channel.wechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
public class RinkoChannelWechatApplication {
    public static void main(String[] args) {
        SpringApplication.run(RinkoChannelWechatApplication.class, args);
    }
}
```

**WechatAdapter.java:**
```java
package com.rinko.channel.wechat.adapter;

import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.wechat.config.WechatProperties;
import com.rinko.channel.management.ChannelManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rinko.channel.wechat.enabled", havingValue = "true")
public class WechatAdapter {
    private static final Logger log = LoggerFactory.getLogger(WechatAdapter.class);
    private final WechatProperties properties;
    private final ChannelBot wechatBot;
    private final ChannelManager channelManager;

    public WechatAdapter(WechatProperties properties, ChannelBot wechatBot, ChannelManager channelManager) {
        this.properties = properties;
        this.wechatBot = wechatBot;
        this.channelManager = channelManager;
    }

    @PostConstruct
    public void connect() {
        log.info("WeChat adapter initializing for appId {}", properties.getAppId());
        channelManager.register(properties.getAppId(), wechatBot);
    }

    @PreDestroy
    public void disconnect() {
        channelManager.unregister(properties.getAppId());
    }
}
```

**WechatChannelBot.java:**
```java
package com.rinko.channel.wechat.bot;

import com.rinko.channel.bot.AbstractChannelBot;
import com.rinko.channel.bot.BotContext;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.user.PlatformUserId;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class WechatChannelBot extends AbstractChannelBot {

    @Override public String getPlatform() { return "WECHAT"; }

    @Override
    public CompletableFuture<String> send(PlatformUserId recipient, RichMessage message, String channelId) {
        log.info("[WeChat] Sending to {}: {}", recipient.platformUserId(), message.fallbackText());
        return CompletableFuture.completedFuture("stub-msg-" + System.currentTimeMillis());
    }

    @Override protected void doStart(BotContext context) { log.info("[WeChat] Starting"); }
    @Override protected void doStop() { log.info("[WeChat] Stopping"); }
}
```

**WechatMessageConverter.java:**
```java
package com.rinko.channel.wechat.converter;

import com.rinko.channel.message.BlockType;
import com.rinko.channel.message.MessageConverter;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.message.TextBlock;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WechatMessageConverter implements MessageConverter<Map<String, Object>> {

    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<String, Object>> getNativeType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    @Override
    public RichMessage toRichMessage(Map<String, Object> nativeMsg) {
        String content = (String) nativeMsg.getOrDefault("Content", "");
        String msgId = (String) nativeMsg.getOrDefault("MsgId", "unknown");
        return RichMessage.builder()
            .messageId(msgId)
            .addBlock(TextBlock.markdown(content))
            .fallbackText(content)
            .build();
    }

    @Override
    public Map<String, Object> toNativeMessage(RichMessage richMessage) {
        String text = richMessage.fallbackText();
        if (text == null) {
            text = richMessage.blocks().stream()
                .filter(b -> b instanceof TextBlock)
                .map(b -> ((TextBlock) b).text())
                .findFirst().orElse("");
        }
        return Map.of(
            "MsgType", "text",
            "Content", text
        );
    }

    @Override
    public boolean supportsBlock(BlockType type) {
        // WeChat Official Account only supports text, image, voice, video, music, news
        return switch (type) {
            case TEXT, IMAGE -> true;
            default -> false;
        };
    }
}
```

- [ ] **Step 4: Write application.yml**

```yaml
spring:
  application:
    name: rinko-channel-wechat
  config:
    import:
      - optional:nacos:application-dev.yml
      - optional:nacos:rinko-channel-wechat-dev.yml
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER:127.0.0.1:18848}
        namespace: ${NACOS_NAMESPACE:rinko}
        group: anchorage
        username: nacos
        password: isincere.
      config:
        server-addr: ${NACOS_SERVER:127.0.0.1:18848}
        namespace: ${NACOS_NAMESPACE:rinko}
        group: anchorage
        username: nacos
        password: isincere.
        file-extension: yml
server:
  port: 8091

rinko:
  channel:
    wechat:
      enabled: false
      app-id: ${WECHAT_APP_ID:}
      app-secret: ${WECHAT_APP_SECRET:}
      token: ${WECHAT_TOKEN:}
      encoding-aes-key: ${WECHAT_AES_KEY:}
```

- [ ] **Step 5: Verify compile**

Run: `./mvnw compile -pl rinko-channel-wechat -am`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add rinko-channel-wechat/ pom.xml
git commit -m "feat: add rinko-channel-wechat module scaffold

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 15: Create rinko-channel-dingtalk module scaffold

**Files:**
- Create: `rinko-channel-dingtalk/pom.xml`
- Create: `rinko-channel-dingtalk/src/main/java/com/rinko/channel/dingtalk/RinkoChannelDingtalkApplication.java`
- Create: `rinko-channel-dingtalk/src/main/java/com/rinko/channel/dingtalk/config/DingtalkProperties.java`
- Create: `rinko-channel-dingtalk/src/main/java/com/rinko/channel/dingtalk/adapter/DingtalkAdapter.java`
- Create: `rinko-channel-dingtalk/src/main/java/com/rinko/channel/dingtalk/bot/DingtalkChannelBot.java`
- Create: `rinko-channel-dingtalk/src/main/java/com/rinko/channel/dingtalk/converter/DingtalkMessageConverter.java`
- Create: `rinko-channel-dingtalk/src/main/resources/application.yml`
- Modify: `pom.xml`

- [ ] **Step 1: Create pom.xml and directory structure**

Create `rinko-channel-dingtalk/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.rinko</groupId>
        <artifactId>rinko-ac</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>rinko-channel-dingtalk</artifactId>
    <packaging>jar</packaging>

    <name>rinko-channel-dingtalk</name>
    <description>Rinko 钉钉机器人通道</description>

    <dependencies>
        <dependency>
            <groupId>com.rinko</groupId>
            <artifactId>rinko-channel</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Automatic-Module-Name>com.rinko.channel.dingtalk</Automatic-Module-Name>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

- [ ] **Step 2: Add module to root POM**

Edit `pom.xml` — add `<module>rinko-channel-dingtalk</module>` after `rinko-channel-wechat`.

- [ ] **Step 3: Write Java classes**

**DingtalkProperties.java:**
```java
package com.rinko.channel.dingtalk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel.dingtalk")
public class DingtalkProperties {
    private boolean enabled = false;
    private String appKey;
    private String appSecret;
    private String robotCode;
}
```

**RinkoChannelDingtalkApplication.java:**
```java
package com.rinko.channel.dingtalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
public class RinkoChannelDingtalkApplication {
    public static void main(String[] args) {
        SpringApplication.run(RinkoChannelDingtalkApplication.class, args);
    }
}
```

**DingtalkAdapter.java:**
```java
package com.rinko.channel.dingtalk.adapter;

import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.dingtalk.config.DingtalkProperties;
import com.rinko.channel.management.ChannelManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rinko.channel.dingtalk.enabled", havingValue = "true")
public class DingtalkAdapter {
    private static final Logger log = LoggerFactory.getLogger(DingtalkAdapter.class);
    private final DingtalkProperties properties;
    private final ChannelBot dingtalkBot;
    private final ChannelManager channelManager;

    public DingtalkAdapter(DingtalkProperties properties, ChannelBot dingtalkBot, ChannelManager channelManager) {
        this.properties = properties;
        this.dingtalkBot = dingtalkBot;
        this.channelManager = channelManager;
    }

    @PostConstruct
    public void connect() {
        log.info("DingTalk adapter initializing for robot {}", properties.getRobotCode());
        channelManager.register(properties.getRobotCode(), dingtalkBot);
    }

    @PreDestroy
    public void disconnect() {
        channelManager.unregister(properties.getRobotCode());
    }
}
```

**DingtalkChannelBot.java:**
```java
package com.rinko.channel.dingtalk.bot;

import com.rinko.channel.bot.AbstractChannelBot;
import com.rinko.channel.bot.BotContext;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.user.PlatformUserId;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class DingtalkChannelBot extends AbstractChannelBot {

    @Override public String getPlatform() { return "DINGTALK"; }

    @Override
    public CompletableFuture<String> send(PlatformUserId recipient, RichMessage message, String channelId) {
        log.info("[DingTalk] Sending to {}: {}", recipient.platformUserId(), message.fallbackText());
        return CompletableFuture.completedFuture("stub-msg-" + System.currentTimeMillis());
    }

    @Override protected void doStart(BotContext context) { log.info("[DingTalk] Starting"); }
    @Override protected void doStop() { log.info("[DingTalk] Stopping"); }
}
```

**DingtalkMessageConverter.java:**
```java
package com.rinko.channel.dingtalk.converter;

import com.rinko.channel.message.BlockType;
import com.rinko.channel.message.MessageConverter;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.message.TextBlock;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DingtalkMessageConverter implements MessageConverter<Map<String, Object>> {

    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<String, Object>> getNativeType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    @Override
    public RichMessage toRichMessage(Map<String, Object> nativeMsg) {
        var textObj = nativeMsg.getOrDefault("text", Map.of());
        String content = "";
        if (textObj instanceof Map<?, ?> textMap) {
            content = (String) textMap.getOrDefault("content", "");
        }
        String msgId = (String) nativeMsg.getOrDefault("msgId", "unknown");
        return RichMessage.builder()
            .messageId(msgId)
            .addBlock(TextBlock.markdown(content))
            .fallbackText(content)
            .build();
    }

    @Override
    public Map<String, Object> toNativeMessage(RichMessage richMessage) {
        String text = richMessage.fallbackText();
        if (text == null) {
            text = richMessage.blocks().stream()
                .filter(b -> b instanceof TextBlock)
                .map(b -> ((TextBlock) b).text())
                .findFirst().orElse("");
        }
        return Map.of("msgtype", "text", "text", Map.of("content", text));
    }

    @Override
    public boolean supportsBlock(BlockType type) {
        // DingTalk supports text, markdown, actionCard, feedCard, link
        return switch (type) {
            case TEXT, IMAGE, ACTION_ROW, DIVIDER -> true;
            case BUTTON -> true; // Supported via actionCard
            case EMBED, SECTION -> true;
        };
    }
}
```

- [ ] **Step 4: Write application.yml**

```yaml
spring:
  application:
    name: rinko-channel-dingtalk
  config:
    import:
      - optional:nacos:application-dev.yml
      - optional:nacos:rinko-channel-dingtalk-dev.yml
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER:127.0.0.1:18848}
        namespace: ${NACOS_NAMESPACE:rinko}
        group: anchorage
        username: nacos
        password: isincere.
      config:
        server-addr: ${NACOS_SERVER:127.0.0.1:18848}
        namespace: ${NACOS_NAMESPACE:rinko}
        group: anchorage
        username: nacos
        password: isincere.
        file-extension: yml
server:
  port: 8092

rinko:
  channel:
    dingtalk:
      enabled: false
      app-key: ${DINGTALK_APP_KEY:}
      app-secret: ${DINGTALK_APP_SECRET:}
      robot-code: ${DINGTALK_ROBOT_CODE:}
```

- [ ] **Step 5: Verify compile**

Run: `./mvnw compile -pl rinko-channel-dingtalk -am`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add rinko-channel-dingtalk/ pom.xml
git commit -m "feat: add rinko-channel-dingtalk module scaffold

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 16: Create rinko-channel-discord module scaffold

**Files:**
- Create: `rinko-channel-discord/pom.xml`
- Create: `rinko-channel-discord/src/main/java/com/rinko/channel/discord/RinkoChannelDiscordApplication.java`
- Create: `rinko-channel-discord/src/main/java/com/rinko/channel/discord/config/DiscordProperties.java`
- Create: `rinko-channel-discord/src/main/java/com/rinko/channel/discord/adapter/DiscordAdapter.java`
- Create: `rinko-channel-discord/src/main/java/com/rinko/channel/discord/bot/DiscordChannelBot.java`
- Create: `rinko-channel-discord/src/main/java/com/rinko/channel/discord/converter/DiscordMessageConverter.java`
- Create: `rinko-channel-discord/src/main/resources/application.yml`
- Modify: `pom.xml`

- [ ] **Step 1: Create pom.xml**

Create `rinko-channel-discord/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.rinko</groupId>
        <artifactId>rinko-ac</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>rinko-channel-discord</artifactId>
    <packaging>jar</packaging>

    <name>rinko-channel-discord</name>
    <description>Rinko Discord 机器人通道</description>

    <dependencies>
        <dependency>
            <groupId>com.rinko</groupId>
            <artifactId>rinko-channel</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Automatic-Module-Name>com.rinko.channel.discord</Automatic-Module-Name>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

- [ ] **Step 2: Add module to root POM**

Edit `pom.xml` — add `<module>rinko-channel-discord</module>` after `rinko-channel-dingtalk`.

- [ ] **Step 3: Write Java classes**

**DiscordProperties.java:**
```java
package com.rinko.channel.discord.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel.discord")
public class DiscordProperties {
    private boolean enabled = false;
    private String botToken;
    private String clientId;
    private List<String> intents = List.of("GUILD_MESSAGES", "MESSAGE_CONTENT");
}
```

**RinkoChannelDiscordApplication.java:**
```java
package com.rinko.channel.discord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
public class RinkoChannelDiscordApplication {
    public static void main(String[] args) {
        SpringApplication.run(RinkoChannelDiscordApplication.class, args);
    }
}
```

**DiscordAdapter.java:**
```java
package com.rinko.channel.discord.adapter;

import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.discord.config.DiscordProperties;
import com.rinko.channel.management.ChannelManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rinko.channel.discord.enabled", havingValue = "true")
public class DiscordAdapter {
    private static final Logger log = LoggerFactory.getLogger(DiscordAdapter.class);
    private final DiscordProperties properties;
    private final ChannelBot discordBot;
    private final ChannelManager channelManager;

    public DiscordAdapter(DiscordProperties properties, ChannelBot discordBot, ChannelManager channelManager) {
        this.properties = properties;
        this.discordBot = discordBot;
        this.channelManager = channelManager;
    }

    @PostConstruct
    public void connect() {
        log.info("Discord adapter initializing for client {}", properties.getClientId());
        channelManager.register(properties.getClientId(), discordBot);
    }

    @PreDestroy
    public void disconnect() {
        channelManager.unregister(properties.getClientId());
    }
}
```

**DiscordChannelBot.java:**
```java
package com.rinko.channel.discord.bot;

import com.rinko.channel.bot.AbstractChannelBot;
import com.rinko.channel.bot.BotContext;
import com.rinko.channel.message.*;
import com.rinko.channel.user.PlatformUserId;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class DiscordChannelBot extends AbstractChannelBot {

    @Override public String getPlatform() { return "DISCORD"; }

    @Override
    public CompletableFuture<String> send(PlatformUserId recipient, RichMessage message, String channelId) {
        log.info("[Discord] Sending to {} in {}: blocks={}",
            recipient.platformUserId(), channelId, message.blocks().size());
        return CompletableFuture.completedFuture("stub-msg-" + System.currentTimeMillis());
    }

    @Override protected void doStart(BotContext context) { log.info("[Discord] Starting"); }
    @Override protected void doStop() { log.info("[Discord] Stopping"); }
}
```

**DiscordMessageConverter.java:**
```java
package com.rinko.channel.discord.converter;

import com.rinko.channel.message.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DiscordMessageConverter implements MessageConverter<Map<String, Object>> {

    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<String, Object>> getNativeType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    @Override
    public RichMessage toRichMessage(Map<String, Object> nativeMsg) {
        // Discord message: { "content": "...", "id": "...", "embeds": [...], ... }
        String content = (String) nativeMsg.getOrDefault("content", "");
        String msgId = (String) nativeMsg.getOrDefault("id", "unknown");
        var builder = RichMessage.builder().messageId(msgId);

        if (content != null && !content.isBlank()) {
            builder.addBlock(TextBlock.markdown(content));
        }

        @SuppressWarnings("unchecked")
        var embeds = (java.util.List<Map<String, Object>>) nativeMsg.get("embeds");
        if (embeds != null) {
            for (var embed : embeds) {
                builder.addBlock(mapEmbed(embed));
            }
        }
        return builder.fallbackText(content).build();
    }

    @Override
    public Map<String, Object> toNativeMessage(RichMessage richMessage) {
        var result = new LinkedHashMap<String, Object>();
        var embeds = new ArrayList<Map<String, Object>>();

        for (var block : richMessage.blocks()) {
            switch (block) {
                case TextBlock tb -> result.put("content", tb.text());
                case EmbedBlock eb -> embeds.add(embedToMap(eb));
                default -> { /* Discord APIs support embeds + text */ }
            }
        }

        if (!embeds.isEmpty()) {
            result.put("embeds", embeds);
        }
        if (richMessage.fallbackText() != null && !result.containsKey("content")) {
            result.put("content", richMessage.fallbackText());
        }
        return result;
    }

    @Override
    public boolean supportsBlock(BlockType type) {
        return switch (type) {
            case TEXT, IMAGE, EMBED, DIVIDER -> true;
            case BUTTON, ACTION_ROW -> true; // Discord supports buttons, select menus via components
            case SECTION -> true;
        };
    }

    private EmbedBlock mapEmbed(Map<String, Object> embed) {
        // Map Discord embed JSON to EmbedBlock
        var title = (String) embed.getOrDefault("title", null);
        var desc = (String) embed.getOrDefault("description", null);
        var url = (String) embed.getOrDefault("url", null);
        var color = embed.get("color") != null
            ? String.format("#%06X", ((Number) embed.get("color")).intValue()) : null;
        return new EmbedBlock(title, desc, url, color, java.util.List.of(), null);
    }

    private Map<String, Object> embedToMap(EmbedBlock eb) {
        var map = new LinkedHashMap<String, Object>();
        if (eb.title() != null) map.put("title", eb.title());
        if (eb.description() != null) map.put("description", eb.description());
        if (eb.url() != null) map.put("url", eb.url());
        if (eb.color() != null) {
            map.put("color", Integer.decode(eb.color()));
        }
        return map;
    }
}
```

- [ ] **Step 4: Write application.yml**

```yaml
spring:
  application:
    name: rinko-channel-discord
  config:
    import:
      - optional:nacos:application-dev.yml
      - optional:nacos:rinko-channel-discord-dev.yml
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER:127.0.0.1:18848}
        namespace: ${NACOS_NAMESPACE:rinko}
        group: anchorage
        username: nacos
        password: isincere.
      config:
        server-addr: ${NACOS_SERVER:127.0.0.1:18848}
        namespace: ${NACOS_NAMESPACE:rinko}
        group: anchorage
        username: nacos
        password: isincere.
        file-extension: yml
server:
  port: 8093

rinko:
  channel:
    discord:
      enabled: false
      bot-token: ${DISCORD_BOT_TOKEN:}
      client-id: ${DISCORD_CLIENT_ID:}
```

- [ ] **Step 5: Verify full project compiles**

Run: `./mvnw compile`
Expected: BUILD SUCCESS for all modules

- [ ] **Step 6: Commit**

```bash
git add rinko-channel-discord/ pom.xml
git commit -m "feat: add rinko-channel-discord module scaffold

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 17: Run all tests and verify

**Files:** None (verification only)

- [ ] **Step 1: Run all rinko-channel tests**

Run: `./mvnw test -pl rinko-channel`
Expected: All tests PASS

- [ ] **Step 2: Run full project compile + test**

Run: `./mvnw test`
Expected: All tests PASS across all modules

- [ ] **Step 3: If any test failures exist, fix them before proceeding**

This is the final quality gate. Do not proceed if any test fails.

---

### Task 18: Final review and commit

**Files:** None (review only)

- [ ] **Step 1: Verify all spec requirements are covered**

Checklist:
- [ ] `ChannelBot` interface with `onEvent`, `send`, `start`, `stop`, `getStatus` ✓
- [ ] `AbstractChannelBot` with lifecycle state machine and event dispatch ✓
- [ ] `ChannelEvent` sealed hierarchy (MessageReceived, MessageSent, UserJoined, UserLeft, Lifecycle) ✓
- [ ] `RichMessage` block-based model with TextBlock, ImageBlock, ButtonBlock, EmbedBlock, SectionBlock, DividerBlock ✓
- [ ] `MessageConverter<T>` bidirectional conversion interface ✓
- [ ] `AiBridge` with `LocalAiBridge` and `RemoteAiBridge` ✓
- [ ] `UnifiedUser` + `PlatformUserId` + `UserMappingService` ✓
- [ ] `ChannelProperties` with nested persistence/compression config ✓
- [ ] `ChannelAutoConfiguration` with Caffeine hot cache ✓
- [ ] `ChannelMessageHistory` + `ConversationSummary` entities with MyBatis-Plus mappers ✓
- [ ] `MessagePersistenceService` with hot/warm/cold routing ✓
- [ ] `MessageCompressionTask` as Spring Bean for rinko-scheduler ✓
- [ ] `ChannelManager` + `ChannelHealthIndicator` + REST controller ✓
- [ ] Four platform modules (QQ, WeChat, DingTalk, Discord) each with adapter, bot, converter, properties ✓
- [ ] Each platform module is independently deployable Spring Boot service ✓

- [ ] **Step 2: Verify file structure matches plan**

Run: `find . -path "*/rinko-channel*" -name "*.java" | grep -v target | sort`

Expected output matches the File Structure Map at the top of this plan.

- [ ] **Step 3: Final commit if any uncommitted changes**

```bash
git status
git add -A
git commit -m "chore: finalize rinko-channel implementation

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```
