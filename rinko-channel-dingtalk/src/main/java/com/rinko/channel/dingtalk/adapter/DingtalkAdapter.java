package com.rinko.channel.dingtalk.adapter;

import com.rinko.channel.bot.BotContext;
import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.dingtalk.config.DingtalkProperties;
import com.rinko.channel.management.ChannelManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        log.info("DingTalk adapter initializing for appKey {}", properties.getAppKey());
        channelManager.register(properties.getAppKey(), dingtalkBot,
            new BotContext("DINGTALK", properties.getAppKey(), Map.of()));
    }

    @PreDestroy
    public void disconnect() {
        channelManager.unregister(properties.getAppKey());
    }
}
