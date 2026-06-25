package com.rinko.channel.ai;

import com.rinko.channel.config.ChannelProperties;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.user.UnifiedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnMissingBean(name = "localAiBridge")
public class RemoteAiBridge implements AiBridge {

    private static final Logger log = LoggerFactory.getLogger(RemoteAiBridge.class);
    private final RestTemplate restTemplate;
    private final ChannelProperties properties;

    public RemoteAiBridge(ChannelProperties properties) {
        this.properties = properties;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public CompletableFuture<RichMessage> generateReply(
        UnifiedUser user,
        RichMessage currentMessage,
        List<String> recentHistory,
        List<String> summaries
    ) {
        String aiUrl = properties.getAi().getRemoteUrl();

        return CompletableFuture.supplyAsync(() -> {
            try {
                var body = Map.of(
                    "message", currentMessage.fallbackText() != null
                        ? currentMessage.fallbackText() : "",
                    "sessionId", "channel-" + user.getId()
                );
                @SuppressWarnings("unchecked")
                var response = restTemplate.postForObject(
                    aiUrl + "/api/ai/chat", body, Map.class);
                if (response != null && response.containsKey("content")) {
                    return RichMessage.textOnly((String) response.get("content"));
                }
                return RichMessage.textOnly("I'm not sure how to respond.");
            } catch (Exception e) {
                log.error("Remote AI call failed for user {}", user.getId(), e);
                return RichMessage.textOnly("Sorry, I'm having trouble right now.");
            }
        });
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForObject(properties.getAi().getRemoteUrl() + "/api/ai/health", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
