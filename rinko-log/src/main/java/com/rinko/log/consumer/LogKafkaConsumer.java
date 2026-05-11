package com.rinko.log.consumer;

import com.rinko.log.dto.LogMessage;
import com.rinko.log.service.LogIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka 日志消费者，批量接收各微服务投递的结构化 JSON 日志。
 */
@Component
public class LogKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(LogKafkaConsumer.class);

    private final LogIngestionService logIngestionService;

    public LogKafkaConsumer(LogIngestionService logIngestionService) {
        this.logIngestionService = logIngestionService;
    }

    @KafkaListener(
            topics = "rinko-logs",
            groupId = "rinko-log-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLogs(List<LogMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        log.debug("Received {} log messages from Kafka", messages.size());
        logIngestionService.ingestBatch(messages);
    }
}
