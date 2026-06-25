package com.rinko.channel.qq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel.qq")
public class QqProperties {
    private boolean enabled = false;
    private String botId;
    private String clientSecret;
    private String apiBaseUrl = "https://api.sgroup.qq.com";
}
