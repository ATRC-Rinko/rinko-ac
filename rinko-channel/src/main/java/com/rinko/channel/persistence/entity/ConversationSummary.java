package com.rinko.channel.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("channel_conversation_summary")
public class ConversationSummary {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long unifiedUserId;
    private String platformType;
    private String summaryText;
    private Integer originalMessageCount;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private LocalDateTime compressedAt;
}
