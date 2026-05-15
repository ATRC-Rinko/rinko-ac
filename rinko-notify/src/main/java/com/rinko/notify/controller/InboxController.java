package com.rinko.notify.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.notify.model.vo.NotificationHistoryVO;
import com.rinko.notify.model.vo.UnreadCountVO;
import com.rinko.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ApiResponse<List<NotificationHistoryVO>> getInbox(
            @RequestParam String userId,
            @RequestParam(required = false) Boolean isRead) {
        return ApiResponse.success(notifyService.getInbox(userId, isRead).stream()
                .map(NotificationHistoryVO::from)
                .toList());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读消息数")
    public ApiResponse<UnreadCountVO> getUnreadCount(@RequestParam String userId) {
        return ApiResponse.success(new UnreadCountVO(notifyService.getUnreadCount(userId)));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记已读")
    public ApiResponse<Void> markRead(@PathVariable long notificationId) {
        notifyService.markRead(notificationId);
        return ApiResponse.success(null);
    }
}
