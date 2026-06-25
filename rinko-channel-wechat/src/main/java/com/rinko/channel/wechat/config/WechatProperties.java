package com.rinko.channel.wechat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel.wechat")
public class WechatProperties {
    private boolean enabled = false;
    private String appId;
    private String appSecret;
    private String token;
    private String encodingAesKey;
}
