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
