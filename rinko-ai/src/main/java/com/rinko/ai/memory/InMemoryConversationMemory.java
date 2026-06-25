package com.rinko.ai.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于内存的 {@link ConversationMemory} 实现。
 * <p>
 * 后续可扩展 Redis / PostgreSQL 实现以支持分布式部署。
 */
public class InMemoryConversationMemory implements ConversationMemory {

    private static final Logger log = LoggerFactory.getLogger(InMemoryConversationMemory.class);

    private final Map<String, List<Message>> store = new ConcurrentHashMap<>();
    private final int maxHistory;

    public InMemoryConversationMemory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public InMemoryConversationMemory() {
        this(50);
    }

    @Override
    public Mono<Void> append(String sessionId, String role, String content) {
        return Mono.fromRunnable(() -> {
            List<Message> history = store.computeIfAbsent(sessionId,
                    k -> new CopyOnWriteArrayList<>());
            history.add(Message.of(role, content));

            // 保留最近 N 条
            while (history.size() > maxHistory) {
                history.remove(0);
            }
            log.debug("Memory append: session={}, role={}, size={}", sessionId, role, history.size());
        });
    }

    @Override
    public Mono<List<Message>> getHistory(String sessionId) {
        return Mono.fromCallable(() -> {
            List<Message> history = store.get(sessionId);
            return history != null ? new ArrayList<>(history) : List.of();
        });
    }

    @Override
    public Mono<List<Message>> getRecentHistory(String sessionId, int limit) {
        return Mono.fromCallable(() -> {
            List<Message> history = store.get(sessionId);
            if (history == null || history.isEmpty()) {
                return List.of();
            }
            int from = Math.max(0, history.size() - limit);
            return new ArrayList<>(history.subList(from, history.size()));
        });
    }

    @Override
    public Mono<Void> clear(String sessionId) {
        return Mono.fromRunnable(() -> {
            store.remove(sessionId);
            log.debug("Memory cleared: session={}", sessionId);
        });
    }
}
