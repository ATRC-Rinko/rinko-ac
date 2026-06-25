package com.rinko.channel.message;

public record TextBlock(
    String text,
    TextStyle textStyle
) implements Block {
    @Override public BlockType type() { return BlockType.TEXT; }
    public enum TextStyle { PLAIN, MARKDOWN, BOLD, ITALIC }
    public static TextBlock plain(String text) { return new TextBlock(text, TextStyle.PLAIN); }
    public static TextBlock markdown(String text) { return new TextBlock(text, TextStyle.MARKDOWN); }
}
