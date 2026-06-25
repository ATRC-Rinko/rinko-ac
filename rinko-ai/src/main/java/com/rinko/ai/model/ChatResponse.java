package com.rinko.ai.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 对话响应 DTO。
 *
 * @param sessionId 会话 ID
 * @param content   回复内容
 * @param toolCalls 工具调用记录（可选）
 * @param usage     Token 用量（可选）
 * @param timestamp 时间戳
 */
public record ChatResponse(
        String sessionId,
        String content,
        java.util.List<ToolCallRecord> toolCalls,
        TokenUsage usage,
        LocalDateTime timestamp
) {

    public static ChatResponse of(String sessionId, String content) {
        return new ChatResponse(sessionId, content, null, null, LocalDateTime.now());
    }

    public static ChatResponse of(String sessionId, String content, TokenUsage usage) {
        return new ChatResponse(sessionId, content, null, usage, LocalDateTime.now());
    }

    /**
     * 工具调用记录。
     */
    public record ToolCallRecord(
            String toolName,
            Map<String, Object> arguments,
            String result
    ) {
    }

    /**
     * Token 用量统计。
     */
    public record TokenUsage(
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {
    }
}
