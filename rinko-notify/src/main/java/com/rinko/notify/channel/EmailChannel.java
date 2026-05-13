package com.rinko.notify.channel;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rinko.notify.entity.NotificationHistory;
import com.rinko.notify.repository.NotificationHistoryMapper;
import com.rinko.notify.provider.EmailProvider;
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
            historyMapper.update(null, new LambdaUpdateWrapper<NotificationHistory>()
                    .eq(NotificationHistory::getId, history.getId())
                    .set(NotificationHistory::getStatus, "SENT")
                    .set(NotificationHistory::getErrorMessage, null));
        } catch (Exception e) {
            log.error("Email send failed: {}", e.getMessage());
            historyMapper.update(null, new LambdaUpdateWrapper<NotificationHistory>()
                    .eq(NotificationHistory::getId, history.getId())
                    .set(NotificationHistory::getStatus, "FAILED")
                    .set(NotificationHistory::getErrorMessage, e.getMessage()));
        }
    }
}
