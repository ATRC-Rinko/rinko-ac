package com.rinko.channel.message;

import java.util.List;

public record EmbedBlock(
    String title, String description, String url, String color,
    List<EmbedField> fields, EmbedThumbnail thumbnail
) implements Block {
    @Override public BlockType type() { return BlockType.EMBED; }
    public record EmbedField(String name, String value, boolean inline) {}
    public record EmbedThumbnail(String url, int width, int height) {}
}
