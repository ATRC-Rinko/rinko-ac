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
