package com.rinko.infra.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS 跨域配置属性。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "rinko.cors")
public class CorsProperties {

    /**
     * 是否启用 CORS
     */
    private boolean enabled = false;
    /**
     * 允许的源
     */
    private List<String> allowedOrigins = List.of();
    /**
     * 允许的 HTTP 方法
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
    /**
     * 允许的请求头
     */
    private List<String> allowedHeaders = List.of();
    /**
     * 是否允许携带凭证
     */
    private boolean allowCredentials = false;
    /**
     * 预检请求缓存时间（秒）
     */
    private long maxAge = 3600;
}
