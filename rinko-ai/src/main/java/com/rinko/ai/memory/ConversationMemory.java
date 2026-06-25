package com.rinko.ai.memory;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 对话历史记忆接口。
 * <p>
 * 支持 in-memory、Redis、PostgreSQL 等多种后端存储。
 */
public interface ConversationMemory {

    /**
     * 添加一条对话记录。
     *
     * @param sessionId 会话 ID
     * @param role      角色（user / assistant）
     * @param content   内容
     */
    Mono<Void> append(String sessionId, String role, String content);

    /**
     * 获取会话的全部历史。
     */
    Mono<List<Message>> getHistory(String sessionId);

    /**
     * 获取最近 N 条历史。
     */
    Mono<List<Message>> getRecentHistory(String sessionId, int limit);

    /**
     * 清除会话历史。
     */
    Mono<Void> clear(String sessionId);

    /**
     * 对话消息模型。
     */
    record Message(String role, String content, long timestamp) {
        public static Message of(String role, String content) {
            return new Message(role, content, System.currentTimeMillis());
        }
    }
}
