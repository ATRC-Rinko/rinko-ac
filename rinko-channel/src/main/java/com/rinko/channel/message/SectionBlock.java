package com.rinko.channel.message;

public record SectionBlock(TextBlock text, Block accessory) implements Block {
    @Override public BlockType type() { return BlockType.SECTION; }
}
