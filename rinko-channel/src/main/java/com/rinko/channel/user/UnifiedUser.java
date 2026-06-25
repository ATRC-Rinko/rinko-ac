package com.rinko.channel.user;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UnifiedUser {
    private Long id;
    private List<String> linkedIdentities = new ArrayList<>();
    private String defaultPlatformType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
