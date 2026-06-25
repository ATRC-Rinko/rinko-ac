package com.rinko.channel.persistence;

import com.rinko.channel.ai.AiBridge;
import com.rinko.channel.persistence.entity.ConversationSummary;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("messageCompressionTask")
public class MessageCompressionTask {

    private static final Logger log = LoggerFactory.getLogger(MessageCompressionTask.class);

    private final MessagePersistenceService persistenceService;
    private final ConversationSummaryMapper summaryMapper;
    @Autowired(required = false)
    private AiBridge aiBridge;

    public MessageCompressionTask(
        MessagePersistenceService persistenceService,
        ConversationSummaryMapper summaryMapper
    ) {
        this.persistenceService = persistenceService;
        this.summaryMapper = summaryMapper;
    }

    /**
     * Entry point for rinko-scheduler BeanJobExecutor.
     * Invoked via reflection: beanName=messageCompressionTask, methodName=compress
     */
    public String compress() {
        if (aiBridge == null || !aiBridge.isAvailable()) {
            log.warn("Compression skipped: AiBridge not available");
            return "SKIPPED: AiBridge not available";
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<ChannelMessageHistory> messages = persistenceService.findUncompressedMessages(cutoff, 500);

        if (messages.isEmpty()) {
            log.info("No messages to compress (cutoff: {})", cutoff);
            return "OK: No messages to compress";
        }

        // Group by sender
        Map<String, List<ChannelMessageHistory>> grouped = messages.stream()
            .collect(Collectors.groupingBy(m ->
                m.getPlatformType() + ":" + m.getSenderId()));

        int groupsCompressed = 0;
        int totalMessages = 0;

        for (var entry : grouped.entrySet()) {
            var batch = entry.getValue();
            if (batch.isEmpty()) continue;

            try {
                // Generate summary text
                String summaryText = generateSimpleSummary(batch);
                var rangeStart = batch.get(0).getCreatedAt();
                var rangeEnd = batch.get(batch.size() - 1).getCreatedAt();
                var ids = batch.stream().map(ChannelMessageHistory::getId).toList();

                // Save summary
                var summary = new ConversationSummary();
                summary.setSummaryText(summaryText);
                summary.setOriginalMessageCount(batch.size());
                summary.setRangeStart(rangeStart);
                summary.setRangeEnd(rangeEnd);
                summary.setCompressedAt(LocalDateTime.now());
                summary.setPlatformType(batch.get(0).getPlatformType());
                summaryMapper.insert(summary);

                // Mark originals as compressed
                persistenceService.markCompressed(ids, summary.getId());

                groupsCompressed++;
                totalMessages += batch.size();
            } catch (Exception e) {
                log.error("Failed to compress batch for {}", entry.getKey(), e);
            }
        }

        String result = String.format(
            "Compressed %d groups, %d messages, cutoff=%s",
            groupsCompressed, totalMessages, cutoff
        );
        log.info(result);
        return result;
    }

    private String generateSimpleSummary(List<ChannelMessageHistory> messages) {
        var texts = messages.stream()
            .map(ChannelMessageHistory::getMessageText)
            .filter(t -> t != null && !t.isBlank())
            .limit(50)
            .toList();
        return "Conversation with " + texts.size() + " messages: "
            + String.join(" | ", texts.stream().limit(5).toList());
    }
}
