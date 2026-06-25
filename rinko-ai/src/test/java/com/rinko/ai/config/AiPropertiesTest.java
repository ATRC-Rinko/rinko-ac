package com.rinko.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfig.class);

    @TestConfiguration
    @EnableConfigurationProperties(AiProperties.class)
    static class PropertiesConfig {
    }

    @Test
    void shouldBindDefaultValues() {
        contextRunner.run(ctx -> {
            AiProperties props = ctx.getBean(AiProperties.class);
            assertThat(props.isEnabled()).isTrue();
            assertThat(props.getModel()).isEqualTo("dashscope:qwen-plus");
            assertThat(props.getAgent().getName()).isEqualTo("rinko-assistant");
            assertThat(props.getAgent().getSysPrompt()).isEqualTo("You are a helpful AI assistant.");
            assertThat(props.getAgent().getCompactionTriggerMessages()).isEqualTo(30);
            assertThat(props.getAgent().getCompactionKeepMessages()).isEqualTo(10);
            assertThat(props.getMemory().getMaxHistory()).isEqualTo(50);
            assertThat(props.getRag().getChunkSize()).isEqualTo(500);
            assertThat(props.getRag().getTopK()).isEqualTo(5);
        });
    }

    @Test
    void shouldBindCustomModel() {
        contextRunner
                .withPropertyValues(
                        "rinko.ai.model=openai:gpt-4o-mini",
                        "rinko.ai.agent.name=test-agent",
                        "rinko.ai.agent.sys-prompt=You are a test assistant."
                )
                .run(ctx -> {
                    AiProperties props = ctx.getBean(AiProperties.class);
                    assertThat(props.getModel()).isEqualTo("openai:gpt-4o-mini");
                    assertThat(props.getAgent().getName()).isEqualTo("test-agent");
                    assertThat(props.getAgent().getSysPrompt()).isEqualTo("You are a test assistant.");
                });
    }

    @Test
    void shouldBindAnthropicModel() {
        contextRunner
                .withPropertyValues("rinko.ai.model=anthropic:claude-sonnet-4-6")
                .run(ctx -> {
                    AiProperties props = ctx.getBean(AiProperties.class);
                    assertThat(props.getModel()).isEqualTo("anthropic:claude-sonnet-4-6");
                });
    }

    @Test
    void shouldRespectDisabledFlag() {
        contextRunner
                .withPropertyValues("rinko.ai.enabled=false")
                .run(ctx -> {
                    // 当 enabled=false 时不创建 AgentScope Bean
                    assertThat(ctx.containsBean("harnessAgent")).isFalse();
                });
    }
}
