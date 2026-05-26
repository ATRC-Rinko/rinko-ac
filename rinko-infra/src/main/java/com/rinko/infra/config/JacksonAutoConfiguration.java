package com.rinko.infra.config;

import com.rinko.infra.config.deser.BaseLocalDateTimeDeserializer;
import com.rinko.infra.config.ser.BaseLocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.core.Version;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleSerializers;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

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
        jsonMapperBuilder.addModules(new JacksonModule() {
            @Override
            public String getModuleName() {
                return "JDKLocalDateTimeModule";
            }

            @Override
            public Version version() {
                return Version.unknownVersion();
            }

            @Override
            public void setupModule(SetupContext context) {
                context.addDeserializers(new SimpleDeserializers()
                        .addDeserializer(LocalDateTime.class, BaseLocalDateTimeDeserializer.of(format)));

                context.addSerializers(new SimpleSerializers()
                        .addSerializer(LocalDateTime.class, BaseLocalDateTimeSerializer.of(format)));
            }
        });
    }
}
