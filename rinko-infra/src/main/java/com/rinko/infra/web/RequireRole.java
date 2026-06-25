package com.rinko.infra.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注需要特定角色才能访问的 Controller 方法。
 * 由 AuthInterceptor 在拦截器中检查。
 * <p>
 * 示例: {@code @RequireRole("ADMIN")}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * 需要的角色名称（不含 ROLE_ 前缀）。
     */
    String value();
}
