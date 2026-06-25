package com.rinko.ai.rag;

import com.rinko.ai.model.ChatRequest;
import com.rinko.ai.model.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * RAG 知识库服务接口。
 * <p>
 * 提供文档索引与知识库问答能力。默认为 in-memory 实现，
 * 启用 pgvector 后切换到 PostgreSQL 向量存储。
 */
public interface KnowledgeBaseService {

    /**
     * 索引文档内容。
     *
     * @param content  文档文本
     * @param metadata 元数据（来源、标题等）
     */
    Mono<Void> index(String content, Map<String, Object> metadata);

    /**
     * 批量索引。
     */
    Mono<Void> indexBatch(List<Document> documents);

    /**
     * 基于知识库的问答。
     */
    Mono<ChatResponse> ask(ChatRequest request);

    /**
     * 基于知识库的流式问答。
     */
    Flux<String> askStream(ChatRequest request);

    /**
     * 文档模型。
     */
    record Document(String content, Map<String, Object> metadata) {
        public Document(String content) {
            this(content, Map.of());
        }
    }
}
