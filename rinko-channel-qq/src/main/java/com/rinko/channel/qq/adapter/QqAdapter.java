package com.rinko.channel.qq.adapter;

import com.rinko.channel.bot.BotContext;
import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.qq.config.QqProperties;
import com.rinko.channel.management.ChannelManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        channelManager.register(properties.getBotId(), qqBot,
            new BotContext("QQ", properties.getBotId(), Map.of()));
        log.info("QQ adapter registered bot {}", properties.getBotId());
    }

    @PreDestroy
    public void disconnect() {
        channelManager.unregister(properties.getBotId());
        log.info("QQ adapter disconnected");
    }
}
