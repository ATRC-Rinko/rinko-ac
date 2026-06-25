package com.rinko.ai.rag;

import com.rinko.ai.agent.ChatAgentService;
import com.rinko.ai.model.ChatRequest;
import com.rinko.ai.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于内存的 {@link KnowledgeBaseService} 实现，使用简单关键词匹配检索。
 * <p>
 * 启用 pgvector 后可替换为语义向量检索实现。
 */
public class InMemoryKnowledgeBaseService implements KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryKnowledgeBaseService.class);

    private final ChatAgentService chatAgentService;
    private final List<Document> documents = new ArrayList<>();

    public InMemoryKnowledgeBaseService(ChatAgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    @Override
    public Mono<Void> index(String content, Map<String, Object> metadata) {
        return Mono.fromRunnable(() -> {
            documents.add(new Document(content, metadata));
            log.debug("Document indexed, total: {}", documents.size());
        });
    }

    @Override
    public Mono<Void> indexBatch(List<Document> docs) {
        return Mono.fromRunnable(() -> {
            documents.addAll(docs);
            log.info("Batch indexed {} documents, total: {}", docs.size(), documents.size());
        });
    }

    @Override
    public Mono<ChatResponse> ask(ChatRequest request) {
        String context = retrieveContext(request.message());
        String augmentedMessage = buildAugmentedPrompt(request.message(), context);
        return chatAgentService.chat(new ChatRequest(augmentedMessage, request.sessionId(), request.context()));
    }

    @Override
    public Flux<String> askStream(ChatRequest request) {
        String context = retrieveContext(request.message());
        String augmentedMessage = buildAugmentedPrompt(request.message(), context);
        return chatAgentService.chatStream(new ChatRequest(augmentedMessage, request.sessionId(), request.context()));
    }

    /**
     * 简单关键词匹配检索（后续可替换为 pgvector 语义检索）。
     */
    private String retrieveContext(String query) {
        if (documents.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String lowerQuery = query.toLowerCase();
        int matched = 0;

        for (Document doc : documents) {
            if (doc.content().toLowerCase().contains(lowerQuery) ||
                    lowerQuery.contains(doc.content().toLowerCase().substring(0, Math.min(50, doc.content().length())))) {
                sb.append("---\n");
                if (doc.metadata().containsKey("title")) {
                    sb.append("标题: ").append(doc.metadata().get("title")).append("\n");
                }
                sb.append(doc.content()).append("\n");
                matched++;
                if (matched >= 5) break; // Top-K = 5
            }
        }

        log.debug("Retrieved {} documents for query", matched);
        return sb.toString();
    }

    private String buildAugmentedPrompt(String question, String context) {
        if (context.isEmpty()) {
            return question;
        }
        return """
                请根据以下参考资料回答问题。如果参考资料不足以回答，请如实说明。

                ## 参考资料
                %s

                ## 问题
                %s
                """.formatted(context, question);
    }
}
