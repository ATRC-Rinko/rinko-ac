package com.rinko.notify.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rinko.notify")
public class NotifyProperties {

    private String emailProvider = "smtp";
    private String smsProvider = "aliyun";

    private Channels channels = new Channels();
    private Smtp smtp = new Smtp();
    private SendGrid sendgrid = new SendGrid();
    private AliyunSms aliyunSms = new AliyunSms();
    private TencentSms tencentSms = new TencentSms();

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

    @Getter @Setter
    public static class Smtp {
        private String host = "smtp.example.com";
        private int port = 587;
        private String username;
        private String password;
        private String from;
    }

    @Getter @Setter
    public static class SendGrid {
        private String apiKey;
        private String from;
    }

    @Getter @Setter
    public static class AliyunSms {
        private String accessKeyId;
        private String accessKeySecret;
        private String signName;
        private String templateCode;
    }

    @Getter @Setter
    public static class TencentSms {
        private String appId;
        private String appKey;
        private String signName;
        private String templateId;
    }
}
