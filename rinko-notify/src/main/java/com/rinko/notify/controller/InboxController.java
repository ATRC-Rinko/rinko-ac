package com.rinko.notify.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.notify.entity.NotificationHistory;
import com.rinko.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notify/inbox")
@Tag(name = "Inbox", description = "站内信接口")
public class InboxController {

    private final NotifyService notifyService;

    public InboxController(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @GetMapping
    @Operation(summary = "查询站内信")
    public ApiResponse<List<NotificationHistory>> getInbox(
            @RequestParam String userId,
            @RequestParam(required = false) Boolean isRead) {
        return ApiResponse.success(notifyService.getInbox(userId, isRead));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读消息数")
    public ApiResponse<Map<String, Long>> getUnreadCount(@RequestParam String userId) {
        return ApiResponse.success(Map.of("count", notifyService.getUnreadCount(userId)));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记已读")
    public ApiResponse<Void> markRead(@PathVariable long notificationId) {
        notifyService.markRead(notificationId);
        return ApiResponse.success(null);
    }
}
