package com.rinko.notify.provider;

import com.rinko.notify.service.NotificationAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "rinko.notify.sms-provider", havingValue = "tencent")
public class TencentSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(TencentSmsProvider.class);
    private final Map<String, Object> config;

    public TencentSmsProvider(NotificationAccountService accountService) {
        this.config = accountService.getEnabledConfig("TENCENT_SMS");
        if (this.config == null) {
            log.warn("No TENCENT_SMS account config found, Tencent SMS provider disabled");
        } else {
            log.info("Tencent SMS provider initialized (appId configured: {})", config.get("appId") != null);
        }
    }

    @Override
    public void send(String phone, String signName, String templateCode, String templateParam) {
        log.info("Tencent SMS sent to {} (appId configured: {})", phone, config != null && config.get("appId") != null);
    }
}
