package com.rinko.channel.qq.bot;

import com.rinko.channel.bot.AbstractChannelBot;
import com.rinko.channel.bot.BotContext;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.user.PlatformUserId;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class QqChannelBot extends AbstractChannelBot {

    @Override public String getPlatform() { return "QQ"; }

    @Override
    public CompletableFuture<String> send(PlatformUserId recipient, RichMessage message, String channelId) {
        log.info("[QQ] Sending message to {} in {}: {}",
            recipient.platformUserId(), channelId, message.fallbackText());
        return CompletableFuture.completedFuture("stub-msg-id-" + System.currentTimeMillis());
    }

    @Override protected void doStart(BotContext context) {
        log.info("[QQ] Bot starting with context: {}", context);
    }

    @Override protected void doStop() {
        log.info("[QQ] Bot stopping");
    }
}
