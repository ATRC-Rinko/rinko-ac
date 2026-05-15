package com.rinko.notify.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.notify")
public class NotifyProperties {

    private String emailProvider = "smtp";
    private String smsProvider = "aliyun";

    private Channels channels = new Channels();

    @Getter @Setter
    public static class Channels {
        private ChannelConfig email = new ChannelConfig(true);
        private ChannelConfig sms = new ChannelConfig(false);
        private ChannelConfig inApp = new ChannelConfig(true);
    }

    @Getter @Setter
    public static class ChannelConfig {
        private boolean enabled;
        public ChannelConfig() {}
        public ChannelConfig(boolean enabled) { this.enabled = enabled; }
    }
}
