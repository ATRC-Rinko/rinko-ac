package com.rinko.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.json.JsonMapper;

import java.text.SimpleDateFormat;

/**
 * Jackson 序列化自动配置。
 * 基于 Jackson 3.0 tools.jackson API，统一 ObjectMapper 行为：
 * 日期格式 yyyy-MM-dd HH:mm:ss、忽略未知属性。
 */
@AutoConfiguration
public class JacksonAutoConfiguration implements JsonMapperBuilderCustomizer {

    @Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
    private String format;

    /**
     * Customize the JsonMapper.Builder.
     *
     * @param jsonMapperBuilder the builder to customize
     */
    @Override
    public void customize(JsonMapper.Builder jsonMapperBuilder) {
        jsonMapperBuilder.defaultDateFormat(new SimpleDateFormat(format));
    }
}
