package com.rinko.channel.message;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RichMessageTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeAndDeserializeRichMessage() throws Exception {
        var msg = RichMessage.builder()
            .messageId("msg-1")
            .addBlock(TextBlock.plain("Hello"))
            .addBlock(new DividerBlock())
            .addBlock(new ImageBlock("https://example.com/img.png", "alt"))
            .fallbackText("Hello")
            .build();
        String json = objectMapper.writeValueAsString(msg);
        var parsed = objectMapper.readValue(json, RichMessage.class);
        assertThat(parsed.blocks()).hasSize(3);
        assertThat(parsed.blocks().get(0)).isInstanceOf(TextBlock.class);
        assertThat(parsed.blocks().get(1)).isInstanceOf(DividerBlock.class);
        assertThat(parsed.blocks().get(2)).isInstanceOf(ImageBlock.class);
        assertThat(((TextBlock) parsed.blocks().get(0)).text()).isEqualTo("Hello");
    }

    @Test
    void shouldCreateTextOnlyQuickly() {
        var msg = RichMessage.textOnly("Quick reply");
        assertThat(msg.blocks()).hasSize(1);
        assertThat(msg.fallbackText()).isEqualTo("Quick reply");
    }
}
