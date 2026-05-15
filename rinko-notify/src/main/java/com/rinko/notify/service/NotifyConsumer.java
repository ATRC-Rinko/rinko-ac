package com.rinko.notify.service;

import com.rinko.notify.channel.EmailChannel;
import com.rinko.notify.channel.InAppChannel;
import com.rinko.notify.channel.NotificationChannel;
import com.rinko.notify.channel.SmsChannel;
import com.rinko.notify.model.entity.NotificationHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotifyConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotifyConsumer.class);
    private final Map<String, NotificationChannel> channels;
    private final RedisTemplate<String, Object> redisTemplate;

    public NotifyConsumer(InAppChannel inApp, EmailChannel email, SmsChannel sms, RedisTemplate<String, Object> redisTemplate) {
        this.channels = Map.of("IN_APP", inApp, "EMAIL", email, "SMS", sms);
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = "notify.queue")
    public void handle(NotificationHistory history) {
        NotificationChannel channel = channels.get(history.getChannel());
        if (channel == null) {
            log.warn("Unknown channel: {}", history.getChannel());
            return;
        }
        log.info("Dispatching notification {} via {}", history.getId(), history.getChannel());
        channel.send(history);

        if ("IN_APP".equals(history.getChannel())) {
            redisTemplate.convertAndSend("notify:push:" + history.getRecipient(), history);
        }
    }
}
