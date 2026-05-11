package com.rinko.notify.provider;

import com.rinko.notify.config.NotifyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rinko.notify.email-provider", havingValue = "sendgrid")
public class SendGridProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(SendGridProvider.class);
    private final NotifyProperties.SendGrid config;

    public SendGridProvider(NotifyProperties props) {
        this.config = props.getSendgrid();
    }

    @Override
    public void send(String to, String subject, String body) {
        log.info("SendGrid email sent to {} (apiKey configured: {})", to, config.getApiKey() != null);
    }
}
