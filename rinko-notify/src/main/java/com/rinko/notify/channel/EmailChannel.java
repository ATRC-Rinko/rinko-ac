package com.rinko.notify.channel;

import com.rinko.notify.entity.NotificationHistory;
import com.rinko.notify.repository.NotificationHistoryMapper;
import com.rinko.notify.provider.EmailProvider;
import com.rinko.notify.config.NotifyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailChannel.class);
    private final EmailProvider emailProvider;
    private final NotificationHistoryMapper historyMapper;

    public EmailChannel(EmailProvider emailProvider, NotificationHistoryMapper historyMapper) {
        this.emailProvider = emailProvider;
        this.historyMapper = historyMapper;
    }

    @Override
    public String getType() { return "EMAIL"; }

    @Override
    public void send(NotificationHistory history) {
        try {
            historyMapper.insert(history);
            emailProvider.send(history.getRecipient(), history.getSubject(), history.getContent());
            historyMapper.updateStatus(history.getId(), "SENT", null);
        } catch (Exception e) {
            log.error("Email send failed: {}", e.getMessage());
            historyMapper.updateStatus(history.getId(), "FAILED", e.getMessage());
        }
    }
}
