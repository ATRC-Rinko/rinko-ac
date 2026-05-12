package com.rinko.infra.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import org.apache.skywalking.apm.toolkit.log.logback.v1.x.logstash.SkyWalkingContextJsonProvider;
import org.apache.skywalking.apm.toolkit.log.logback.v1.x.logstash.TraceIdJsonProvider;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Logback JSON 日志编码器。
 * 输出格式：{ "timestamp": "...", "level": "...", "service": "...", "traceId":
 * "...", ... }
 */
public class JsonEncoder extends EncoderBase<ILoggingEvent> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneOffset.UTC);

    private String serviceName = "rinko-ac";

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("timestamp", ISO_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp())));
        logMap.put("level", event.getLevel().toString());
        logMap.put("service", serviceName);
        logMap.put("traceId", event.getLoggerContextVO().getPropertyMap().getOrDefault(TraceIdJsonProvider.TRACING_ID, "N/A"));
        logMap.put("spanId", event.getLoggerContextVO().getPropertyMap().getOrDefault(SkyWalkingContextJsonProvider.SKYWALKING_CONTEXT, "N/A"));
        logMap.put("class", event.getLoggerName());
        logMap.put("message", event.getFormattedMessage());
        logMap.put("thread", event.getThreadName());

        // 追加 MDC context
        Map<String, String> mdcMap = event.getMDCPropertyMap();
        Map<String, Object> context = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : mdcMap.entrySet()) {
            if (!"traceId".equals(entry.getKey()) && !"spanId".equals(entry.getKey())) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
        logMap.put("context", context);

        // 追加异常信息
        if (event.getThrowableProxy() != null) {
            logMap.put("exception", event.getThrowableProxy().getMessage());
            logMap.put("exceptionClass", event.getThrowableProxy().getClassName());
        }

        // 简单序列化为 JSON-like 字符串
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, Object> entry : logMap.entrySet()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append("\"").append(entry.getKey()).append("\": ");
            appendValue(sb, entry.getValue());
        }
        sb.append("}");
        sb.append(System.lineSeparator());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Number) {
            sb.append(value);
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Map) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append("\"").append(e.getKey()).append("\": ");
                appendValue(sb, e.getValue());
                first = false;
            }
            sb.append("}");
        } else {
            sb.append("\"").append(escapeJson(value.toString())).append("\"");
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }
}
