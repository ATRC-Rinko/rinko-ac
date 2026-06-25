package com.rinko.channel.message;

import java.util.List;

public record ActionRowBlock(List<ButtonBlock> buttons) implements Block {
    @Override public BlockType type() { return BlockType.ACTION_ROW; }
    public ActionRowBlock { buttons = List.copyOf(buttons); }
}
