package com.rinko.channel.discord.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel.discord")
public class DiscordProperties {
    private boolean enabled = false;
    private String botToken;
    private String clientId;
    private List<String> intents = List.of("GUILD_MESSAGES", "MESSAGE_CONTENT");
}
