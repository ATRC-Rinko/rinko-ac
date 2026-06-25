package com.rinko.channel.message;

public record ButtonBlock(
    String text, String actionId, ButtonStyle style, String url
) implements Block {
    @Override public BlockType type() { return BlockType.BUTTON; }
    public enum ButtonStyle { PRIMARY, DANGER, DEFAULT }
    public static ButtonBlock primary(String text, String actionId) { return new ButtonBlock(text, actionId, ButtonStyle.PRIMARY, null); }
    public static ButtonBlock link(String text, String url) { return new ButtonBlock(text, null, ButtonStyle.DEFAULT, url); }
}
