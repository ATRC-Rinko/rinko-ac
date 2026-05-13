package com.rinko.infra.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Setter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Logback Appender that sends log events to Kafka.
 * Uses the shared {@link JsonEncoder} for formatting and
 * {@link KafkaLogAppenderHolder} to obtain the KafkaTemplate.
 * @author rinko
 */
public class KafkaLogAppender extends AppenderBase<ILoggingEvent> {

    @Setter
    private String topic = "rinko-logs";
    private final JsonEncoder encoder = new JsonEncoder();
    ObjectMapper objectMapper  = JsonMapper.builder()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    public void setServiceName(String serviceName) {
        encoder.setServiceName(serviceName);
        encoder.start();
    }

    @Override
    public void start() {
        encoder.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        encoder.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        KafkaTemplate<String, String> template = KafkaLogAppenderHolder.getKafkaTemplate();
        if (template == null) {
            addError("KafkaTemplate not available — is KafkaLogAppenderHolder initialized?");
            return;
        }
        byte[] bytes = encoder.encode(event);
        String payload = new String(bytes, StandardCharsets.UTF_8);
        try {
            CompletableFuture<SendResult<String, String>> future = template.send(topic, payload);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    addWarn("Failed to send log to Kafka topic " + topic + ": " + ex.getMessage());
                }
            });
        } catch (Exception e) {
            addWarn("Failed to enqueue log to Kafka topic " + topic + ": " + e.getMessage());
        }
    }
}
