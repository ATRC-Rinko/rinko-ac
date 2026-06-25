package com.rinko.infra.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 认证拦截器自动配置（仅 Servlet/WebMVC 服务）。
 * Gateway 已完成 JWT 验证并注入 X-User-Id / X-User-Roles Header，
 * 内部服务通过此拦截器读取 Header，Controller 从 request 属性获取用户身份。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthAutoConfiguration implements WebMvcConfigurer {

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/send-code");
    }
}
