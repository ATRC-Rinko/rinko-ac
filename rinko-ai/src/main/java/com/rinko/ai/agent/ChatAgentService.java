package com.rinko.ai.agent;

import com.rinko.ai.model.ChatRequest;
import com.rinko.ai.model.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式 AI 对话服务接口。
 * <p>
 * 提供基础对话、流式对话、会话管理等能力，供 WebFlux 模块（rinko-auth、rinko-gateway）注入使用。
 */
public interface ChatAgentService {

    /**
     * 同步对话（一次性返回完整回复）。
     */
    Mono<ChatResponse> chat(ChatRequest request);

    /**
     * 流式对话（SSE），逐 token 推送。
     */
    Flux<String> chatStream(ChatRequest request);

    /**
     * 重置指定会话的上下文。
     */
    Mono<Void> resetSession(String sessionId);
}
