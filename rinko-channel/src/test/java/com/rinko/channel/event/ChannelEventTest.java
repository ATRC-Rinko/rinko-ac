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
