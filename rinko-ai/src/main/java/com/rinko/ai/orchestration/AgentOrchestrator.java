package com.rinko.ai.orchestration;

import com.rinko.ai.model.ChatRequest;
import com.rinko.ai.model.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 多 Agent 编排器接口。
 * <p>
 * 支持定义子 Agent 协作流程，将复杂任务拆解为多个 Agent 协同完成。
 */
public interface AgentOrchestrator {

    /**
     * 注册子 Agent。
     *
     * @param name        子 Agent 名称
     * @param systemPrompt 系统提示词（定义角色和能力）
     */
    void registerAgent(String name, String systemPrompt);

    /**
     * 注销子 Agent。
     */
    void unregisterAgent(String name);

    /**
     * 执行编排任务 — 同步返回。
     *
     * @param task    任务描述
     * @param context 上下文参数
     */
    Mono<ChatResponse> execute(String task, Map<String, Object> context);

    /**
     * 执行编排任务 — 流式返回。
     */
    Flux<String> executeStream(String task, Map<String, Object> context);

    /**
     * 列出所有已注册的子 Agent。
     */
    Map<String, String> listAgents();
}
