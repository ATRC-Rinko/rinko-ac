package com.rinko.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Rinko AI 配置属性。
 *
 * <p>前缀：rinko.ai，模型使用字符串形式由 AgentScope ModelRegistry 解析，
 * 自动读取对应环境变量（DASHSCOPE_API_KEY / OPENAI_API_KEY 等）。</p>
 *
 * <p>示例：
 * <pre>{@code
 * rinko.ai.model=dashscope:qwen-plus
 * rinko.ai.model=openai:gpt-4o
 * rinko.ai.model=deepseek:deepseek-chat
 * rinko.ai.model=anthropic:claude-sonnet-4-6
 * }</pre>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.ai")
public class AiProperties {

    /** 是否启用 AI 能力，默认 true */
    private boolean enabled = true;

    /** 模型字符串，由 ModelRegistry 解析，如 dashscope:qwen-plus */
    private String model = "dashscope:qwen-plus";

    private Agent agent = new Agent();
    private Distributed distributed = new Distributed();
    private Filesystem filesystem = new Filesystem();
    private Sandbox sandbox = new Sandbox();
    private Rag rag = new Rag();
    private Memory memory = new Memory();

    @Getter
    @Setter
    public static class Agent {
        /** Agent 名称 */
        private String name = "rinko-assistant";
        /** 系统提示词 */
        private String sysPrompt = "You are a helpful AI assistant.";
        /** 工作区路径 */
        private String workspace = "./data/ai/workspace";
        /** 触发压缩的消息数阈值 */
        private int compactionTriggerMessages = 30;
        /** 压缩后保留的消息数 */
        private int compactionKeepMessages = 10;
    }

    @Getter
    @Setter
    public static class Distributed {
        /** 分布式存储类型：none | redis | mysql */
        private String storeType = "none";
        private Redis redis = new Redis();
        private Mysql mysql = new Mysql();

        @Getter @Setter
        public static class Redis {
            /** Jedis 连接 URI */
            private String uri = "redis://localhost:6379";
            /** Key 前缀 */
            private String keyPrefix = "agentscope:";
        }

        @Getter @Setter
        public static class Mysql {
            /** 使用已有的 DataSource bean 名称 */
            private String dataSourceBean = "dataSource";
        }
    }

    @Getter
    @Setter
    public static class Filesystem {
        /** 文件系统模式：local | remote | sandbox */
        private String mode = "local";
        /** 隔离范围：session | user | agent | global */
        private String isolationScope = "user";
        /** 匿名用户 fallback */
        private String anonymousUserId = "_default";
    }

    @Getter
    @Setter
    public static class Sandbox {
        /** 沙箱类型：docker | kubernetes */
        private String type = "docker";
        /** Docker 镜像 */
        private String image = "ubuntu:24.04";
    }

    @Getter
    @Setter
    public static class Rag {
        private boolean enabled = false;
        /** 向量存储类型：in-memory */
        private String vectorStore = "in-memory";
        /** 文档分块大小 */
        private int chunkSize = 500;
        /** 检索 Top-K */
        private int topK = 5;
    }

    @Getter
    @Setter
    public static class Memory {
        /** 记忆存储：in-memory | redis */
        private String type = "in-memory";
        /** 对话历史最大条数 */
        private int maxHistory = 50;
    }
}
