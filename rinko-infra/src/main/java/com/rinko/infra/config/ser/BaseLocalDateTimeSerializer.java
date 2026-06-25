package com.rinko.infra.config.ser;

import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author lbb
 * @date 2026/4/30 14:50
 *
 */
public class BaseLocalDateTimeSerializer extends LocalDateTimeSerializer {
    private static final ConcurrentHashMap<String, BaseLocalDateTimeSerializer>
            CACHE = new ConcurrentHashMap<>();

    public BaseLocalDateTimeSerializer(DateTimeFormatter formatter) {
        super(formatter);
    }

    public static BaseLocalDateTimeSerializer of(String format) {
        return CACHE.computeIfAbsent(
                format,
                f -> new BaseLocalDateTimeSerializer(
                        DateTimeFormatter.ofPattern(f))
        );
    }
}
