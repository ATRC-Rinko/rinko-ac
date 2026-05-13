package com.rinko.notify.channel;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rinko.notify.entity.NotificationHistory;
import com.rinko.notify.repository.NotificationHistoryMapper;
import com.rinko.notify.provider.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rinko.notify.channels.sms.enabled", havingValue = "true")
public class SmsChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(SmsChannel.class);
    private final SmsProvider smsProvider;
    private final NotificationHistoryMapper historyMapper;

    public SmsChannel(SmsProvider smsProvider, NotificationHistoryMapper historyMapper) {
        this.smsProvider = smsProvider;
        this.historyMapper = historyMapper;
    }

    @Override
    public String getType() { return "SMS"; }

    @Override
    public void send(NotificationHistory history) {
        try {
            historyMapper.insert(history);
            smsProvider.send(history.getRecipient(), "Rinko", history.getTemplateCode(), history.getContent());
            historyMapper.update(null, new LambdaUpdateWrapper<NotificationHistory>()
                    .eq(NotificationHistory::getId, history.getId())
                    .set(NotificationHistory::getStatus, "SENT")
                    .set(NotificationHistory::getErrorMessage, null));
        } catch (Exception e) {
            log.error("SMS send failed: {}", e.getMessage());
            historyMapper.update(null, new LambdaUpdateWrapper<NotificationHistory>()
                    .eq(NotificationHistory::getId, history.getId())
                    .set(NotificationHistory::getStatus, "FAILED")
                    .set(NotificationHistory::getErrorMessage, e.getMessage()));
        }
    }
}
