package com.rinko.channel.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextBlock.class, name = "TEXT"),
    @JsonSubTypes.Type(value = ImageBlock.class, name = "IMAGE"),
    @JsonSubTypes.Type(value = ButtonBlock.class, name = "BUTTON"),
    @JsonSubTypes.Type(value = ActionRowBlock.class, name = "ACTION_ROW"),
    @JsonSubTypes.Type(value = EmbedBlock.class, name = "EMBED"),
    @JsonSubTypes.Type(value = SectionBlock.class, name = "SECTION"),
    @JsonSubTypes.Type(value = DividerBlock.class, name = "DIVIDER")
})
public sealed interface Block
    permits TextBlock, ImageBlock, ButtonBlock, ActionRowBlock,
            EmbedBlock, SectionBlock, DividerBlock {

    BlockType type();
}
