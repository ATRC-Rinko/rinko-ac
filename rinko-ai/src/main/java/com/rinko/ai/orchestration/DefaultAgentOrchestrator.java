package com.rinko.ai.orchestration;

import com.rinko.ai.agent.ChatAgentService;
import com.rinko.ai.model.ChatRequest;
import com.rinko.ai.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link AgentOrchestrator} 默认实现。
 * <p>
 * 基于主 Agent + 子 Agent 注册模式：将复杂任务交给主 Agent 规划，
 * 主 Agent 按需调用子 Agent 完成子任务，最终汇总结果。
 */
public class DefaultAgentOrchestrator implements AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DefaultAgentOrchestrator.class);

    private final ChatAgentService chatAgentService;
    private final Map<String, String> subAgents = new ConcurrentHashMap<>();

    private static final String ORCHESTRATOR_SYS_PROMPT = """
            You are a task orchestration agent. Your role is to:
            1. Analyze the user's task and decompose it into sub-tasks.
            2. For each sub-task, specify which sub-agent should handle it.
            3. Synthesize the results into a coherent final response.
            
            Available sub-agents:
            %s
            
            Format your response as a clear, structured answer that addresses the user's original request.
            """;

    public DefaultAgentOrchestrator(ChatAgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    @Override
    public void registerAgent(String name, String systemPrompt) {
        subAgents.put(name, systemPrompt);
        log.info("Registered sub-agent: {}", name);
    }

    @Override
    public void unregisterAgent(String name) {
        subAgents.remove(name);
        log.info("Unregistered sub-agent: {}", name);
    }

    @Override
    public Mono<ChatResponse> execute(String task, Map<String, Object> context) {
        String agentList = buildAgentList();
        String prompt = ORCHESTRATOR_SYS_PROMPT.formatted(agentList) + "\n\nUser task: " + task;

        return chatAgentService.chat(new ChatRequest(prompt, null, context));
    }

    @Override
    public Flux<String> executeStream(String task, Map<String, Object> context) {
        String agentList = buildAgentList();
        String prompt = ORCHESTRATOR_SYS_PROMPT.formatted(agentList) + "\n\nUser task: " + task;

        return chatAgentService.chatStream(new ChatRequest(prompt, null, context));
    }

    @Override
    public Map<String, String> listAgents() {
        return new LinkedHashMap<>(subAgents);
    }

    private String buildAgentList() {
        if (subAgents.isEmpty()) {
            return "(No sub-agents registered — handle the task directly)";
        }
        StringBuilder sb = new StringBuilder();
        subAgents.forEach((name, prompt) ->
                sb.append("- **").append(name).append("**: ").append(prompt).append("\n"));
        return sb.toString();
    }
}
