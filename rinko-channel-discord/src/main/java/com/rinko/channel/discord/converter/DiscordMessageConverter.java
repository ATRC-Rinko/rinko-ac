package com.rinko.channel.discord.converter;

import com.rinko.channel.message.BlockType;
import com.rinko.channel.message.EmbedBlock;
import com.rinko.channel.message.MessageConverter;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.message.TextBlock;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DiscordMessageConverter implements MessageConverter<Map<String, Object>> {
    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<String, Object>> getNativeType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RichMessage toRichMessage(Map<String, Object> nativeMsg) {
        String content = (String) nativeMsg.getOrDefault("content", "");
        String msgId = (String) nativeMsg.getOrDefault("id", "unknown");

        var builder = RichMessage.builder().messageId(msgId).fallbackText(content);
        if (!content.isBlank()) {
            builder.addBlock(TextBlock.markdown(content));
        }

        Object embedsObj = nativeMsg.get("embeds");
        if (embedsObj instanceof List) {
            for (Object obj : (List<Object>) embedsObj) {
                if (obj instanceof Map) {
                    builder.addBlock(mapEmbed((Map<String, Object>) obj));
                }
            }
        }

        return builder.build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toNativeMessage(RichMessage richMessage) {
        StringBuilder contentBuilder = new StringBuilder();
        List<Map<String, Object>> embeds = new ArrayList<>();

        for (var block : richMessage.blocks()) {
            switch (block) {
                case TextBlock tb -> contentBuilder.append(tb.text()).append("\n");
                case EmbedBlock eb -> embeds.add(embedToMap(eb));
                default -> {}
            }
        }

        String content = contentBuilder.toString().strip();
        if (content.isEmpty() && richMessage.fallbackText() != null) {
            content = richMessage.fallbackText();
        }

        Map<String, Object> result = Map.of("content", content);
        if (!embeds.isEmpty()) {
            result = Map.of("content", content, "embeds", embeds);
        }
        return result;
    }

    @Override
    public boolean supportsBlock(BlockType type) {
        return switch (type) {
            case TEXT, IMAGE, EMBED, DIVIDER, BUTTON, ACTION_ROW, SECTION -> true;
            default -> false;
        };
    }

    @SuppressWarnings("unchecked")
    private EmbedBlock mapEmbed(Map<String, Object> embed) {
        String title = (String) embed.getOrDefault("title", "");
        String description = (String) embed.getOrDefault("description", "");
        String url = (String) embed.getOrDefault("url", "");
        Integer colorInt = (Integer) embed.get("color");
        String color = colorInt != null ? String.format("#%06X", colorInt) : null;
        return new EmbedBlock(title, description, url, color, List.of(), null);
    }

    private Map<String, Object> embedToMap(EmbedBlock eb) {
        var map = new java.util.LinkedHashMap<String, Object>();
        if (eb.title() != null && !eb.title().isBlank()) map.put("title", eb.title());
        if (eb.description() != null && !eb.description().isBlank()) map.put("description", eb.description());
        if (eb.url() != null && !eb.url().isBlank()) map.put("url", eb.url());
        if (eb.color() != null && !eb.color().isBlank()) {
            map.put("color", Integer.decode(eb.color()));
        }
        return map;
    }
}
