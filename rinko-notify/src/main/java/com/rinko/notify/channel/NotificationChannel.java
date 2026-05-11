package com.rinko.notify.channel;

import com.rinko.notify.entity.NotificationHistory;

public interface NotificationChannel {

    String getType();

    void send(NotificationHistory history);
}
