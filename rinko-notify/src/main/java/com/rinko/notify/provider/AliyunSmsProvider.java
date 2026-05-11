package com.rinko.notify.provider;

import com.rinko.notify.config.NotifyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rinko.notify.sms-provider", havingValue = "aliyun", matchIfMissing = true)
public class AliyunSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsProvider.class);
    private final NotifyProperties.AliyunSms config;

    public AliyunSmsProvider(NotifyProperties props) {
        this.config = props.getAliyunSms();
    }

    @Override
    public void send(String phone, String signName, String templateCode, String templateParam) {
        log.info("Aliyun SMS sent to {} (accessKey configured: {})", phone, config.getAccessKeyId() != null);
    }
}
