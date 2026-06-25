package com.rinko.channel.wechat.adapter;

import com.rinko.channel.bot.BotContext;
import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.wechat.config.WechatProperties;
import com.rinko.channel.management.ChannelManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        channelManager.register(properties.getAppId(), wechatBot,
            new BotContext("WECHAT", properties.getAppId(), Map.of()));
    }

    @PreDestroy
    public void disconnect() {
        channelManager.unregister(properties.getAppId());
    }
}
