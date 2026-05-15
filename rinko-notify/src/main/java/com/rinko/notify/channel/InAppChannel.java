package com.rinko.notify.channel;

import com.rinko.notify.model.entity.NotificationHistory;
import com.rinko.notify.repository.NotificationHistoryMapper;
import org.springframework.stereotype.Component;

@Component
public class InAppChannel implements NotificationChannel {

    private final NotificationHistoryMapper historyMapper;

    public InAppChannel(NotificationHistoryMapper historyMapper) {
        this.historyMapper = historyMapper;
    }

    @Override
    public String getType() { return "IN_APP"; }

    @Override
    public void send(NotificationHistory history) {
        historyMapper.insert(history);
    }
}
