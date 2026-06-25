package com.rinko.channel.message;

public record DividerBlock() implements Block {
    @Override public BlockType type() { return BlockType.DIVIDER; }
    public static final DividerBlock INSTANCE = new DividerBlock();
}
