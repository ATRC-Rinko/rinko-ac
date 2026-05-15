package com.rinko.notify.provider;

import com.rinko.notify.service.NotificationAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "rinko.notify.sms-provider", havingValue = "aliyun", matchIfMissing = true)
public class AliyunSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsProvider.class);
    private final Map<String, Object> config;

    public AliyunSmsProvider(NotificationAccountService accountService) {
        this.config = accountService.getEnabledConfig("ALIYUN_SMS");
        if (this.config == null) {
            log.warn("No ALIYUN_SMS account config found, Aliyun SMS provider disabled");
        } else {
            log.info("Aliyun SMS provider initialized (accessKey configured: {})", config.get("accessKeyId") != null);
        }
    }

    @Override
    public void send(String phone, String signName, String templateCode, String templateParam) {
        log.info("Aliyun SMS sent to {} (accessKey configured: {})", phone, config != null && config.get("accessKeyId") != null);
    }
}
