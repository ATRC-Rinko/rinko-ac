package com.rinko.ai.model;

import java.util.Map;

/**
 * AI 对话请求 DTO。
 *
 * @param message  用户消息
 * @param sessionId 会话 ID（可选，为空则新建会话）
 * @param context   附加上下文参数（可选）
 */
public record ChatRequest(
        String message,
        String sessionId,
        Map<String, Object> context
) {
    public ChatRequest {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }

    public ChatRequest(String message) {
        this(message, null, null);
    }

    public ChatRequest(String message, String sessionId) {
        this(message, sessionId, null);
    }
}
