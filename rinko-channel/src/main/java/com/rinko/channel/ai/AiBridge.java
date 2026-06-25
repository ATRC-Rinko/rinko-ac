package com.rinko.channel.ai;

import com.rinko.channel.message.RichMessage;
import com.rinko.channel.user.UnifiedUser;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiBridge {

    CompletableFuture<RichMessage> generateReply(
        UnifiedUser user,
        RichMessage currentMessage,
        List<String> recentHistory,
        List<String> summaries
    );

    boolean isAvailable();
}
