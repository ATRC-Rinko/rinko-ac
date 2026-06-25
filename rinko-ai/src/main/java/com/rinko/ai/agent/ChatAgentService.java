package com.rinko.ai.agent;

import com.rinko.ai.model.ChatRequest;
import com.rinko.ai.model.ChatResponse;
import com.rinko.ai.model.StreamEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式 AI 对话服务接口。
 *
 * <p>提供基础对话、流式对话、会话管理等能力，供 WebFlux 模块（rinko-auth、rinko-gateway）注入使用。
 * 流式方法提供两种粒度：简单文本流（向后兼容）和结构化事件流（推荐）。</p>
 */
public interface ChatAgentService {

    /**
     * 同步对话（一次性返回完整回复）。
     */
    Mono<ChatResponse> chat(ChatRequest request);

    /**
     * 流式对话 — 简单文本流，仅推送模型回复的文本增量。
     * 适用于简单的 SSE 展示场景。
     */
    Flux<String> chatStream(ChatRequest request);

    /**
     * 流式对话 — 结构化事件流，包含文本/思考/工具调用等全部中间事件。
     * 适用于需要展示思考过程、工具调用状态的前端。
     */
    Flux<StreamEvent> chatStreamEvents(ChatRequest request);

    /**
     * 重置指定会话的上下文。
     */
    Mono<Void> resetSession(String sessionId);
}
