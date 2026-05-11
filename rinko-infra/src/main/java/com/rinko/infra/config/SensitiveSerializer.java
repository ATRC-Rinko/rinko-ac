package com.rinko.infra.config;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * 敏感信息 JSON 序列化器。
 * 将标记了 @Sensitive 的字段序列化为 "***"。
 */
public class SensitiveSerializer extends ValueSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext context) throws JacksonException {
        gen.writeString("***");
    }
}
