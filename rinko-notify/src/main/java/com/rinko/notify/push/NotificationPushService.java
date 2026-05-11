package com.rinko.notify.push;

import com.rinko.notify.entity.NotificationHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class NotificationPushService implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationPushService.class);
    private final Map<String, CopyOnWriteArraySet<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();
    private final RedisMessageListenerContainer container;
    private final RedisTemplate<String, Object> redisTemplate;

    public NotificationPushService(RedisMessageListenerContainer container, RedisTemplate<String, Object> redisTemplate) {
        this.container = container;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        container.addMessageListener(this, new ChannelTopic("notify:push:*"));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotificationHistory notif = (NotificationHistory) redisTemplate.getValueSerializer().deserialize(message.getBody());
            if (notif != null) {
                pushToSse(notif.getRecipient(), notif);
            }
        } catch (Exception e) {
            log.warn("Failed to process Redis push message: {}", e.getMessage());
        }
    }

    public SseEmitter createSseEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(300_000L);
        sseEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(emitter);
        emitter.onCompletion(() -> removeSseEmitter(userId, emitter));
        emitter.onTimeout(() -> removeSseEmitter(userId, emitter));
        return emitter;
    }

    private void removeSseEmitter(String userId, SseEmitter emitter) {
        var set = sseEmitters.get(userId);
        if (set != null) set.remove(emitter);
    }

    private void pushToSse(String userId, NotificationHistory notif) {
        var emitters = sseEmitters.get(userId);
        if (emitters == null) return;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notif));
            } catch (IOException e) {
                log.debug("SSE push failed for userId={}", userId);
            }
        }
    }
}
