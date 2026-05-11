package com.rinko.infra.config;

import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感信息标记注解。
 * 标记了此注解的字段在 JSON 序列化时将被替换为 "***"。
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JsonSerialize(using = SensitiveSerializer.class)
public @interface Sensitive {
}
