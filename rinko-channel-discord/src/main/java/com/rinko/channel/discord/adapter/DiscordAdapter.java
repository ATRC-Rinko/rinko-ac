package com.rinko.channel.discord.adapter;

import com.rinko.channel.bot.BotContext;
import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.discord.config.DiscordProperties;
import com.rinko.channel.management.ChannelManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "rinko.channel.discord.enabled", havingValue = "true")
public class DiscordAdapter {
    private static final Logger log = LoggerFactory.getLogger(DiscordAdapter.class);
    private final DiscordProperties properties;
    private final ChannelBot discordBot;
    private final ChannelManager channelManager;

    public DiscordAdapter(DiscordProperties properties, ChannelBot discordBot, ChannelManager channelManager) {
        this.properties = properties;
        this.discordBot = discordBot;
        this.channelManager = channelManager;
    }

    @PostConstruct
    public void connect() {
        log.info("Discord adapter initializing for clientId {}", properties.getClientId());
        channelManager.register(properties.getClientId(), discordBot,
            new BotContext("DISCORD", properties.getClientId(), Map.of()));
    }

    @PreDestroy
    public void disconnect() {
        channelManager.unregister(properties.getClientId());
    }
}
