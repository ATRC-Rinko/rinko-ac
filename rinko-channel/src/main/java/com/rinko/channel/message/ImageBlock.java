package com.rinko.channel.message;

public record ImageBlock(
    String imageUrl, String altText, int width, int height
) implements Block {
    @Override public BlockType type() { return BlockType.IMAGE; }
    public ImageBlock(String imageUrl, String altText) { this(imageUrl, altText, 0, 0); }
}
