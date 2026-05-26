package com.rinko.infra.config.deser;

import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author lbb
 * @date 2026/4/30 14:53
 *
 */
public class BaseLocalDateTimeDeserializer extends LocalDateTimeDeserializer {

    private static final ConcurrentHashMap<String, BaseLocalDateTimeDeserializer>
            CACHE = new ConcurrentHashMap<>();

    public BaseLocalDateTimeDeserializer(DateTimeFormatter formatter) {
        super(formatter);
    }

    public static BaseLocalDateTimeDeserializer of(String format) {
        return CACHE.computeIfAbsent(
                format,
                f -> new BaseLocalDateTimeDeserializer(
                        DateTimeFormatter.ofPattern(f))
        );
    }
}