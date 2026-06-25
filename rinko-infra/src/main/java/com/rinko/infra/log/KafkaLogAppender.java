package com.rinko.infra.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Setter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Logback Appender，使用原生 KafkaProducer 直接发送日志到 Kafka。
 * 不依赖 Spring 容器，Logback 初始化时即创建 producer。
 */
public class KafkaLogAppender extends AppenderBase<ILoggingEvent> {

    private static final String DEFAULT_BOOTSTRAP_SERVERS = "kafka:9092";

    @Setter
    private String topic = "rinko-logs";
    @Setter
    private String bootstrapServers = DEFAULT_BOOTSTRAP_SERVERS;

    private final JsonEncoder encoder = new JsonEncoder();
    private KafkaProducer<String, String> producer;

    public void setServiceName(String serviceName) {
        encoder.setServiceName(serviceName);
        encoder.start();
    }

    @Override
    public void start() {
        encoder.start();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, "3");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "5000");
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "10000");

        producer = new KafkaProducer<>(props);
        // 预拉取 metadata，避免第一条日志 send 时阻塞
        try {
            producer.partitionsFor(topic);
            producer.flush();
        } catch (Exception ignored) {
            // metadata 拉取失败不阻止启动，后续 send 时还会重试
        }
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        if (producer != null) {
            producer.close();
            producer = null;
        }
        encoder.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted() || producer == null) {
            return;
        }
        byte[] bytes = encoder.encode(event);
        String payload = new String(bytes, StandardCharsets.UTF_8);
        producer.send(new ProducerRecord<>(topic, payload), (metadata, ex) -> {
            if (ex != null) {
                addWarn("Failed to send log to Kafka: " + ex.getMessage());
            }
        });
    }
}
