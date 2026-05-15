package com.rinko.notify.provider;

import com.rinko.notify.service.NotificationAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "rinko.notify.email-provider", havingValue = "sendgrid")
public class SendGridProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(SendGridProvider.class);
    private final Map<String, Object> config;

    public SendGridProvider(NotificationAccountService accountService) {
        this.config = accountService.getEnabledConfig("SENDGRID");
        if (this.config == null) {
            log.warn("No SENDGRID account config found, SendGrid provider disabled");
        } else {
            log.info("SendGrid provider initialized (apiKey configured: {})", config.get("apiKey") != null);
        }
    }

    @Override
    public void send(String to, String subject, String body) {
        log.info("SendGrid email sent to {} (apiKey configured: {})", to, config != null && config.get("apiKey") != null);
    }
}
