package com.rinko.ai.config;

import com.rinko.ai.agent.ChatAgentService;
import com.rinko.ai.agent.DefaultChatAgentService;
import com.rinko.ai.memory.ConversationMemory;
import com.rinko.ai.memory.InMemoryConversationMemory;
import com.rinko.ai.orchestration.AgentOrchestrator;
import com.rinko.ai.orchestration.DefaultAgentOrchestrator;
import com.rinko.ai.rag.InMemoryKnowledgeBaseService;
import com.rinko.ai.rag.KnowledgeBaseService;
import com.rinko.ai.tool.DateTimeTool;
import com.rinko.ai.tool.JsonTool;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * AgentScope 2.0 自动装配。
 *
 * <p>按 rinko.ai.enabled 控制是否启用，模型使用字符串形式由 ModelRegistry 解析。
 * 切换厂商时改 rinko.ai.model 并设置对应的环境变量即可。</p>
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@ConditionalOnProperty(prefix = "rinko.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AgentScopeAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AgentScopeAutoConfiguration.class);

    private final AiProperties properties;

    public AgentScopeAutoConfiguration(AiProperties properties) {
        this.properties = properties;
    }

    // ==================== Toolkit ====================

    @Bean
    @ConditionalOnMissingBean
    public Toolkit agentScopeToolkit() {
        log.info("Registering shared AgentScope tools");
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new DateTimeTool());
        toolkit.registerTool(new JsonTool());
        return toolkit;
    }

    // ==================== Model ====================

    @Bean
    @ConditionalOnMissingBean
    public Model agentScopeModel() {
        String modelStr = properties.getModel();
        log.info("Initializing AgentScope model: {}", modelStr);

        String provider = modelStr.contains(":") ? modelStr.substring(0, modelStr.indexOf(':')) : modelStr;
        String modelName = modelStr.contains(":") ? modelStr.substring(modelStr.indexOf(':') + 1) : modelStr;

        return switch (provider) {
            case "dashscope" -> io.agentscope.core.model.DashScopeChatModel.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .modelName(modelName)
                    .stream(true)
                    .build();
            case "openai" -> OpenAIChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName(modelName)
                    .stream(true)
                    .build();
            case "deepseek" -> OpenAIChatModel.builder()
                    .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                    .modelName(modelName)
                    .baseUrl("https://api.deepseek.com")
                    .stream(true)
                    .build();
            case "anthropic" -> io.agentscope.core.model.AnthropicChatModel.builder()
                    .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                    .modelName(modelName)
                    .stream(true)
                    .build();
            default -> throw new IllegalStateException(
                    "Unsupported model provider: " + provider
                    + ". Supported: dashscope:, openai:, deepseek:, anthropic:.");
        };
    }

    // ==================== HarnessAgent ====================

    @Bean
    @ConditionalOnMissingBean
    public HarnessAgent harnessAgent(Model agentScopeModel, Toolkit agentScopeToolkit) {
        AiProperties.Agent c = properties.getAgent();
        log.info("Creating HarnessAgent: name={}, workspace={}", c.getName(), c.getWorkspace());

        HarnessAgent agent = HarnessAgent.builder()
                .name(c.getName())
                .sysPrompt(c.getSysPrompt())
                .model(agentScopeModel)
                .workspace(Path.of(c.getWorkspace()))
                .toolkit(agentScopeToolkit)
                .compaction(CompactionConfig.builder()
                        .triggerMessages(c.getCompactionTriggerMessages())
                        .keepMessages(c.getCompactionKeepMessages())
                        .build())
                .build();

        log.info("HarnessAgent '{}' initialized successfully", c.getName());
        return agent;
    }

    // ==================== Service Layer ====================

    @Bean
    @ConditionalOnMissingBean
    public ChatAgentService chatAgentService(HarnessAgent harnessAgent) {
        return new DefaultChatAgentService(harnessAgent, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConversationMemory conversationMemory() {
        return new InMemoryConversationMemory(properties.getMemory().getMaxHistory());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rinko.ai.rag", name = "enabled", havingValue = "true")
    public KnowledgeBaseService knowledgeBaseService(ChatAgentService chatAgentService) {
        return new InMemoryKnowledgeBaseService(chatAgentService);
    }

    @Bean
    @ConditionalOnMissingBean
    public AgentOrchestrator agentOrchestrator(ChatAgentService chatAgentService) {
        return new DefaultAgentOrchestrator(chatAgentService);
    }
}
