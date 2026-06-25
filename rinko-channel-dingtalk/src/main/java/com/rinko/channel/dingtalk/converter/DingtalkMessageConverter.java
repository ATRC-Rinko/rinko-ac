package com.rinko.channel.dingtalk.converter;

import com.rinko.channel.message.BlockType;
import com.rinko.channel.message.MessageConverter;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.message.TextBlock;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DingtalkMessageConverter implements MessageConverter<Map<String, Object>> {
    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<String, Object>> getNativeType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    @Override
    public RichMessage toRichMessage(Map<String, Object> nativeMsg) {
        String content = "";
        Object textObj = nativeMsg.get("text");
        if (textObj instanceof Map) {
            content = (String) ((Map<?, ?>) textObj).get("content");
        }
        if (content == null) content = "";
        String msgId = (String) nativeMsg.getOrDefault("msgId", "unknown");
        return RichMessage.builder().messageId(msgId).addBlock(TextBlock.markdown(content)).fallbackText(content).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toNativeMessage(RichMessage richMessage) {
        String text = richMessage.fallbackText();
        if (text == null) {
            text = richMessage.blocks().stream()
                .filter(b -> b instanceof TextBlock).map(b -> ((TextBlock) b).text())
                .findFirst().orElse("");
        }
        return Map.of("msgtype", "text", "text", Map.of("content", text));
    }

    @Override
    public boolean supportsBlock(BlockType type) {
        return switch (type) {
            case TEXT, IMAGE, ACTION_ROW, DIVIDER, BUTTON, EMBED, SECTION -> true;
            default -> false;
        };
    }
}
