package com.rinko.channel.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record RichMessage(
    String messageId, List<Block> blocks, String fallbackText, Map<String, Object> metadata
) {
    public RichMessage {
        blocks = blocks != null ? List.copyOf(blocks) : List.of();
    }

    public static Builder builder() { return new Builder(); }

    public static RichMessage textOnly(String text) {
        return builder().addBlock(TextBlock.markdown(text)).fallbackText(text).build();
    }

    public static class Builder {
        private String messageId;
        private final List<Block> blocks = new ArrayList<>();
        private String fallbackText;
        private Map<String, Object> metadata = Map.of();

        public Builder messageId(String id) { this.messageId = id; return this; }
        public Builder addBlock(Block block) { this.blocks.add(block); return this; }
        public Builder fallbackText(String text) { this.fallbackText = text; return this; }
        public Builder metadata(Map<String, Object> meta) { this.metadata = meta; return this; }
        public RichMessage build() { return new RichMessage(messageId, blocks, fallbackText, metadata); }
    }
}
