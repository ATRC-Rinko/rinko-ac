package com.rinko.infra.log;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


/**
 * @author rinko
 */
@Component
@AutoConfigureAfter(KafkaProducerConfig.class)
public class KafkaLogAppenderHolder {
    private final KafkaTemplate<String, String> template;
    @Getter
    private static KafkaTemplate<String, String> kafkaTemplate;

    public KafkaLogAppenderHolder(KafkaTemplate<String, String> template) {
        this.template = template;
    }

    @PostConstruct
    public void init() {
        kafkaTemplate = this.template;
    }


}
