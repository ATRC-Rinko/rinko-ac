package com.rinko.notify.controller;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.notify.model.dto.SendBatchRequest;
import com.rinko.notify.model.dto.SendNotificationRequest;
import com.rinko.notify.model.vo.NotificationHistoryVO;
import com.rinko.notify.model.vo.SendBatchVo;
import com.rinko.notify.push.NotificationPushService;
import com.rinko.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    public ApiResponse<NotificationHistoryVO> send(@Valid @RequestBody SendNotificationRequest req, HttpServletResponse response) {
        Map<String, String> variables = req.variables() != null ? req.variables() : Map.of();
        var history = notifyService.send(req.channel(), req.templateCode(), req.recipient(), variables);
        response.setStatus(202);
        return ApiResponse.success(NotificationHistoryVO.from(history));
    }

    @PostMapping("/send-batch")
    @Operation(summary = "批量发送通知")
    public ApiResponse<SendBatchVo> sendBatch(@Valid @RequestBody SendBatchRequest req, HttpServletResponse response) {
        Map<String, String> variables = req.variables() != null ? req.variables() : Map.of();
        var result = notifyService.sendBatch(req.channel(), req.templateCode(), req.recipients(), variables);
        response.setStatus(202);
        return ApiResponse.success(new SendBatchVo((int) result.get("count"),
                (java.util.List<Long>) result.get("notificationIds")));
    }

    @GetMapping("/stream")
    @Operation(summary = "SSE 实时通知流")
    public SseEmitter stream(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        return pushService.createSseEmitter(userId);
    }
}
