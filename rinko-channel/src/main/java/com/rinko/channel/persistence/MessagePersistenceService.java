package com.rinko.channel.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessagePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(MessagePersistenceService.class);

    private final ChannelMessageHistoryMapper historyMapper;
    private final Cache<String, Object> hotCache;

    public MessagePersistenceService(
        ChannelMessageHistoryMapper historyMapper,
        Cache<String, Object> hotCache
    ) {
        this.historyMapper = historyMapper;
        this.hotCache = hotCache;
    }

    /** Persist an inbound or outbound message. */
    public void save(ChannelMessageHistory history) {
        history.setCreatedAt(LocalDateTime.now());
        historyMapper.insert(history);
        hotCache.put("msg:" + history.getId(), history);
    }

    /** Get recent messages for a user within a time window (warm tier). */
    public List<ChannelMessageHistory> getRecentMessages(
        String platformType, String senderId, LocalDateTime since) {
        return historyMapper.selectList(new LambdaQueryWrapper<ChannelMessageHistory>()
            .eq(ChannelMessageHistory::getPlatformType, platformType)
            .eq(ChannelMessageHistory::getSenderId, senderId)
            .ge(ChannelMessageHistory::getCreatedAt, since)
            .orderByAsc(ChannelMessageHistory::getCreatedAt));
    }

    /** Find messages older than threshold that have not been compressed. */
    public List<ChannelMessageHistory> findUncompressedMessages(
        LocalDateTime before, int limit) {
        return historyMapper.selectList(new LambdaQueryWrapper<ChannelMessageHistory>()
            .lt(ChannelMessageHistory::getCreatedAt, before)
            .ne(ChannelMessageHistory::getCompressed, true)
            .last("LIMIT " + limit));
    }

    /** Mark a batch of messages as compressed. */
    public void markCompressed(List<Long> messageIds, Long summaryId) {
        for (var id : messageIds) {
            var msg = new ChannelMessageHistory();
            msg.setId(id);
            msg.setCompressed(true);
            msg.setSummaryId(summaryId);
            historyMapper.updateById(msg);
        }
    }

    /** Delete raw messages older than retention days. */
    public int deleteOlderThan(LocalDateTime cutoff) {
        var count = historyMapper.delete(new LambdaQueryWrapper<ChannelMessageHistory>()
            .lt(ChannelMessageHistory::getCreatedAt, cutoff));
        log.info("Deleted {} raw messages older than {}", count, cutoff);
        return count;
    }
}
