package com.rinko.channel.dingtalk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel.dingtalk")
public class DingtalkProperties {
    private boolean enabled = false;
    private String appKey;
    private String appSecret;
    private String robotCode;
}
