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
