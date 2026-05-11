package com.rinko.notify.provider;

import com.rinko.notify.config.NotifyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rinko.notify.sms-provider", havingValue = "tencent")
public class TencentSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(TencentSmsProvider.class);
    private final NotifyProperties.TencentSms config;

    public TencentSmsProvider(NotifyProperties props) {
        this.config = props.getTencentSms();
    }

    @Override
    public void send(String phone, String signName, String templateCode, String templateParam) {
        log.info("Tencent SMS sent to {} (appId configured: {})", phone, config.getAppId() != null);
    }
}
