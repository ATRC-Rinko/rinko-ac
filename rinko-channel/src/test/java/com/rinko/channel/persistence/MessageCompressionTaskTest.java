package com.rinko.channel.persistence;

import com.rinko.channel.ai.AiBridge;
import com.rinko.channel.persistence.entity.ChannelMessageHistory;
import com.rinko.channel.persistence.entity.ConversationSummary;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageCompressionTaskTest {

    @Test
    void shouldCompressMessagesOlderThanWarmWindow() {
        var historyMapper = mock(ChannelMessageHistoryMapper.class);
        var summaryMapper = mock(ConversationSummaryMapper.class);
        var aiBridge = mock(AiBridge.class);
        var persistenceService = new MessagePersistenceService(historyMapper, null);

        var oldMsg = new ChannelMessageHistory();
        oldMsg.setId(1L);
        oldMsg.setPlatformType("DISCORD");
        oldMsg.setSenderId("user-1");
        oldMsg.setMessageText("Hello old message");
        oldMsg.setCompressed(false);
        oldMsg.setCreatedAt(LocalDateTime.now().minusDays(10));

        when(aiBridge.isAvailable()).thenReturn(true);
        when(historyMapper.selectList(any())).thenReturn(List.of(oldMsg));

        var task = new MessageCompressionTask(
            persistenceService,
            summaryMapper
        );
        ReflectionTestUtils.setField(task, "aiBridge", aiBridge);

        String result = task.compress();
        assertThat(result).contains("Compressed");
        verify(historyMapper, atLeastOnce()).updateById(any(ChannelMessageHistory.class));
        verify(summaryMapper, atLeastOnce()).insert(any(ConversationSummary.class));
    }
}
