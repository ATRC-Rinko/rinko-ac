package com.rinko.channel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel")
public class ChannelProperties {
    private Ai ai = new Ai();

    @Getter
    @Setter
    public static class Ai {
        private String remoteUrl = "http://localhost:8083";
    }
}
