package com.rinko.channel.ai;

import com.rinko.ai.agent.ChatAgentService;
import com.rinko.ai.model.ChatRequest;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.message.TextBlock;
import com.rinko.channel.user.UnifiedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@ConditionalOnBean(ChatAgentService.class)
public class LocalAiBridge implements AiBridge {

    private static final Logger log = LoggerFactory.getLogger(LocalAiBridge.class);
    private final ChatAgentService chatAgentService;

    public LocalAiBridge(ChatAgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    @Override
    public CompletableFuture<RichMessage> generateReply(
        UnifiedUser user,
        RichMessage currentMessage,
        List<String> recentHistory,
        List<String> summaries
    ) {
        String userMessage = currentMessage.fallbackText();
        if (userMessage == null && !currentMessage.blocks().isEmpty()) {
            userMessage = currentMessage.blocks().stream()
                .filter(b -> b instanceof TextBlock)
                .map(b -> ((TextBlock) b).text())
                .collect(Collectors.joining("\n"));
        }
        if (userMessage == null || userMessage.isBlank()) {
            userMessage = "Hello";
        }

        ChatRequest request = new ChatRequest(userMessage, "channel-" + user.getId());

        return CompletableFuture.supplyAsync(() -> {
            try {
                var response = chatAgentService.chat(request).block();
                return RichMessage.textOnly(
                    response != null && response.content() != null
                        ? response.content()
                        : "I'm not sure how to respond.");
            } catch (Exception e) {
                log.error("AI generation failed for user {}", user.getId(), e);
                return RichMessage.textOnly("Sorry, I'm having trouble thinking right now.");
            }
        });
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
