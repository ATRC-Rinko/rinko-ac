package com.rinko.infra.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;

/**
 * SkyWalking TraceId 注入 Logback 转换器。
 * 从 SkyWalking TraceContext 自动获取 traceId，未接入时从 MDC 获取或返回 "N/A"。
 */
public class TraceIdConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        // 优先从 MDC 获取 traceId
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }

        // 尝试从 SkyWalking TraceContext 获取（通过反射，避免编译时强依赖）
        try {
            Class<?> traceContextClass = Class.forName("org.apache.skywalking.apm.toolkit.trace.TraceContext");
            Object tid = traceContextClass.getMethod("traceId").invoke(null);
            if (tid != null && !"N/A".equals(tid.toString()) && !tid.toString().isEmpty()) {
                String id = tid.toString();
                MDC.put("traceId", id);
                return id;
            }
        } catch (Exception | NoClassDefFoundError ignored) {
            // SkyWalking 未接入
        }

        return "N/A";
    }
}
