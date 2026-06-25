package com.rinko.channel.bot;

import com.rinko.channel.event.ChannelEvent;
import com.rinko.channel.event.ChannelEvent.MessageReceivedEvent;
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
        protected void doStart(BotContext context) { }

        @Override
        protected void doStop() { }

        public String getLastReceivedMessage() { return lastReceivedMessage; }
    }
}
