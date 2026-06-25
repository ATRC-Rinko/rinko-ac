package com.rinko.channel.management.controller;

import com.rinko.channel.management.ChannelManager;
import com.rinko.channel.model.vo.ChannelStatusVO;
import com.rinko.infra.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/channel")
public class ChannelManageController {

    private final ChannelManager channelManager;

    public ChannelManageController(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @GetMapping("/bots")
    public ApiResponse<List<ChannelStatusVO>> listBots() {
        return ApiResponse.success(channelManager.listBots());
    }

    @PostMapping("/bots/{botId}/start")
    public ApiResponse<Void> startBot(@PathVariable String botId) {
        channelManager.startBot(botId);
        return ApiResponse.success();
    }

    @PostMapping("/bots/{botId}/stop")
    public ApiResponse<Void> stopBot(@PathVariable String botId) {
        channelManager.stopBot(botId);
        return ApiResponse.success();
    }

    @GetMapping("/bots/{botId}/status")
    public ApiResponse<ChannelStatusVO> getBotStatus(@PathVariable String botId) {
        var status = channelManager.getBotStatus(botId);
        if (status == null) {
            return new ApiResponse<>(404, "Bot not found", null, LocalDateTime.now());
        }
        return ApiResponse.success(status);
    }
}
