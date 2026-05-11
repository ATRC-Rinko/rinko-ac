package com.rinko.notify.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.notify.entity.NotificationHistory;
import com.rinko.notify.push.NotificationPushService;
import com.rinko.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notify")
@Tag(name = "Notification", description = "通知发送接口")
public class NotifyController {

    private final NotifyService notifyService;
    private final NotificationPushService pushService;

    public NotifyController(NotifyService notifyService, NotificationPushService pushService) {
        this.notifyService = notifyService;
        this.pushService = pushService;
    }

    @PostMapping("/send")
    @Operation(summary = "发送通知")
    public ApiResponse<NotificationHistory> send(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String channel = (String) body.get("channel");
        String templateCode = (String) body.get("templateCode");
        String recipient = (String) body.get("recipient");
        @SuppressWarnings("unchecked")
        Map<String, String> variables = (Map<String, String>) body.getOrDefault("variables", Map.of());
        NotificationHistory history = notifyService.send(channel, templateCode, recipient, variables);
        response.setStatus(202);
        return ApiResponse.success(history);
    }

    @PostMapping("/send-batch")
    @Operation(summary = "批量发送通知")
    public ApiResponse<Map<String, Object>> sendBatch(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String channel = (String) body.get("channel");
        String templateCode = (String) body.get("templateCode");
        @SuppressWarnings("unchecked")
        List<String> recipients = (List<String>) body.get("recipients");
        @SuppressWarnings("unchecked")
        Map<String, String> variables = (Map<String, String>) body.getOrDefault("variables", Map.of());
        Map<String, Object> result = notifyService.sendBatch(channel, templateCode, recipients, variables);
        response.setStatus(202);
        return ApiResponse.success(result);
    }

    @GetMapping("/stream")
    @Operation(summary = "SSE 实时通知流")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter stream(@RequestParam String userId) {
        return pushService.createSseEmitter(userId);
    }
}
