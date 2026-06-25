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
