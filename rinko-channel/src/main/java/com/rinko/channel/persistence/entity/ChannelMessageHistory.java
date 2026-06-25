package com.rinko.channel.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("channel_message_history")
public class ChannelMessageHistory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String platformType;
    private String senderId;
    private String recipientId;
    private String channelId;
    private String direction;      // INBOUND or OUTBOUND
    private String messageText;
    private String richMessageJson;
    private String status;         // PENDING, SENT, FAILED
    private Boolean compressed;
    private Long summaryId;
    private LocalDateTime createdAt;
}
