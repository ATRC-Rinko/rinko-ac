package com.rinko.ai.agent;

import com.rinko.ai.config.AiProperties;
import com.rinko.ai.model.ChatRequest;
import com.rinko.ai.model.ChatResponse;
import com.rinko.ai.model.StreamEvent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEndEvent;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.event.ToolCallEndEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 AgentScope HarnessAgent 的 {@link ChatAgentService} 默认实现。
 */
public class DefaultChatAgentService implements ChatAgentService {

    private static final Logger log = LoggerFactory.getLogger(DefaultChatAgentService.class);

    private final HarnessAgent agent;
    private final AiProperties properties;
    private final Map<String, AgentSession> sessions = new ConcurrentHashMap<>();

    public DefaultChatAgentService(HarnessAgent agent, AiProperties properties) {
        this.agent = agent;
        this.properties = properties;
    }

    @Override
    public Mono<ChatResponse> chat(ChatRequest request) {
        return Mono.fromCallable(() -> {
            log.debug("Chat request: sessionId={}, message={}", request.sessionId(), request.message());
            String sid = resolveSessionId(request);
            AgentSession session = sessions.computeIfAbsent(sid, AgentSession::new);

            UserMessage userMsg = new UserMessage(request.message());
            RuntimeContext ctx = buildContext(sid);
            var response = agent.call(userMsg, ctx).block();

            String content = response != null ? response.getTextContent() : "";
            session.incrementTurn();
            log.debug("Chat response: sessionId={}, turns={}", sid, session.getTurns());

            return ChatResponse.of(sid, content);
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    @Override
    public Flux<String> chatStream(ChatRequest request) {
        return chatStreamEvents(request)
                .filter(e -> "text".equals(e.type()) || "thinking".equals(e.type()))
                .map(StreamEvent::delta);
    }

    @Override
    public Flux<StreamEvent> chatStreamEvents(ChatRequest request) {
        return Flux.create(sink -> {
            String sid = resolveSessionId(request);
            sessions.computeIfAbsent(sid, AgentSession::new);

            UserMessage userMsg = new UserMessage(request.message());
            RuntimeContext ctx = buildContext(sid);

            agent.streamEvents(userMsg, ctx)
                    .doOnNext(event -> handleEvent(event, sink))
                    .doOnComplete(sink::complete)
                    .doOnError(sink::error)
                    .subscribe();
        });
    }

    private void handleEvent(AgentEvent event, reactor.core.publisher.FluxSink<StreamEvent> sink) {
        AgentEventType type = event.getType();

        // 模型回复的流式文本片段
        if (type == AgentEventType.TEXT_BLOCK_DELTA) {
            String delta = ((TextBlockDeltaEvent) event).getDelta();
            if (delta != null && !delta.isEmpty()) {
                sink.next(StreamEvent.text(delta));
            }
        }
        // 思考/推理过程（DeepSeek-R1、o1 等推理模型）
        else if (type == AgentEventType.THINKING_BLOCK_DELTA) {
            String delta = ((ThinkingBlockDeltaEvent) event).getDelta();
            if (delta != null && !delta.isEmpty()) {
                sink.next(StreamEvent.thinking(delta));
            }
        }
        // 工具调用开始
        else if (type == AgentEventType.TOOL_CALL_START) {
            ToolCallStartEvent tc = (ToolCallStartEvent) event;
            sink.next(StreamEvent.toolCall(tc.getToolCallName(), tc.getToolCallId()));
        }
        // 工具调用结束
        else if (type == AgentEventType.TOOL_CALL_END) {
            ToolCallEndEvent tc = (ToolCallEndEvent) event;
            sink.next(StreamEvent.toolResult(tc.getToolCallId(), tc.getToolCallId()));
        }
        // Agent 执行结束
        else if (type == AgentEventType.AGENT_END) {
            sink.next(StreamEvent.agentEnd());
        }
        // 其他事件类型（MODEL_CALL_START/END, TOOL_RESULT_DELTA 等）按需扩展
    }

    @Override
    public Mono<Void> resetSession(String sessionId) {
        return Mono.fromRunnable(() -> {
            sessions.remove(sessionId);
            log.info("Session reset: {}", sessionId);
        });
    }

    private String resolveSessionId(ChatRequest request) {
        if (request.sessionId() != null && !request.sessionId().isBlank()) {
            return request.sessionId();
        }
        return UUID.randomUUID().toString();
    }

    private RuntimeContext buildContext(String sessionId) {
        return RuntimeContext.builder()
                .sessionId(sessionId)
                .build();
    }

    /**
     * 内部会话状态。
     */
    private static class AgentSession {
        private final String id;
        private int turns;
        private final long createdAt;

        AgentSession(String id) {
            this.id = id;
            this.turns = 0;
            this.createdAt = System.currentTimeMillis();
        }

        void incrementTurn() {
            turns++;
        }

        int getTurns() {
            return turns;
        }
    }
}
