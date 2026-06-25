package com.rinko.notify.service;

import com.rinko.notify.channel.NotificationChannel;
import com.rinko.notify.model.entity.NotificationHistory;
import com.rinko.notify.model.message.NotifyMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotifyConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotifyConsumer.class);
    private final Map<String, NotificationChannel> channels;
    private final RedisTemplate<String, NotificationHistory> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotifyService notifyService;

    public NotifyConsumer(List<NotificationChannel> channels, RedisTemplate<String, NotificationHistory> redisTemplate,
                          ObjectMapper objectMapper, NotifyService notifyService) {
        this.channels = channels.stream()
                .collect(Collectors.toMap(NotificationChannel::getType, channel -> channel));
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.notifyService = notifyService;
    }

    private static final int MAX_MESSAGE_SIZE = 64 * 1024; // 64KB max per message

    @RabbitListener(queues = "notify.queue")
    public void handle(String msg) {
        if (msg == null || msg.length() > MAX_MESSAGE_SIZE) {
            log.warn("Rejected oversized message: {} bytes", msg != null ? msg.length() : 0);
            return;
        }
        NotifyMessageDto message = objectMapper.readValue(msg, NotifyMessageDto.class);
        NotificationChannel channel = channels.get(message.channel());
        if (channel == null) {
            log.warn("Unknown channel: {}", message.channel());
            return;
        }
        log.info("Dispatching notification {} via {}", message.channel(), message.recipient());

        NotificationHistory history = notifyService.coverHistory(message.channel(), message.templateCode(), message.recipient(), message.variables());
        channel.send(history);
        if ("IN_APP".equals(message.channel())) {
            redisTemplate.convertAndSend("notify:push:" + message.recipient(), history);
        }
    }
}
