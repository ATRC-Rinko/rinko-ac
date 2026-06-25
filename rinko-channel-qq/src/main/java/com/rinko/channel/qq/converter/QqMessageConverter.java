package com.rinko.channel.qq.converter;

import com.rinko.channel.message.BlockType;
import com.rinko.channel.message.MessageConverter;
import com.rinko.channel.message.RichMessage;
import com.rinko.channel.message.TextBlock;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QqMessageConverter implements MessageConverter<Map<String, Object>> {

    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<String, Object>> getNativeType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    @Override
    public RichMessage toRichMessage(Map<String, Object> nativeMsg) {
        String content = (String) nativeMsg.getOrDefault("content", "");
        String msgId = (String) nativeMsg.getOrDefault("id", "unknown");
        return RichMessage.builder()
            .messageId(msgId)
            .addBlock(TextBlock.markdown(content))
            .fallbackText(content)
            .build();
    }

    @Override
    public Map<String, Object> toNativeMessage(RichMessage richMessage) {
        String text = richMessage.fallbackText();
        if (text == null) {
            text = richMessage.blocks().stream()
                .filter(b -> b instanceof TextBlock)
                .map(b -> ((TextBlock) b).text())
                .findFirst()
                .orElse("");
        }
        return Map.of(
            "content", text,
            "msg_type", 0,
            "msg_id", richMessage.messageId() != null ? richMessage.messageId() : ""
        );
    }

    @Override
    public boolean supportsBlock(BlockType type) {
        return switch (type) {
            case TEXT, IMAGE, DIVIDER -> true;
            case BUTTON, ACTION_ROW -> false;
            case EMBED, SECTION -> true;
        };
    }
}
