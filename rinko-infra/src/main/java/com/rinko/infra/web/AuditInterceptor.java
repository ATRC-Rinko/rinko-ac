package com.rinko.infra.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 审计拦截器。
 * 记录每个请求的来源 IP、URI、耗时，并通过 MDC 注入 requestId。
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String START_TIME_KEY = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(REQUEST_ID_KEY, requestId);
        request.setAttribute(START_TIME_KEY, System.currentTimeMillis());
        log.info("Request started: {} {} from {}", request.getMethod(), request.getRequestURI(),
                request.getRemoteAddr());
        log.info("X-User-Id: {}", request.getHeader("X-User-Id"));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_KEY);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;
        log.info("Request completed: {} {} status={} duration={}ms",
                request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        MDC.remove(REQUEST_ID_KEY);
        MDC.remove(START_TIME_KEY);
    }
}
