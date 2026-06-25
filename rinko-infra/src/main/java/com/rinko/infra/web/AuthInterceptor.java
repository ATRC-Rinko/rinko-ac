package com.rinko.infra.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器 — 从 Gateway 注入的 Header 读取用户身份。
 * 内部服务信任 Gateway 已完成 JWT 验证，无需重复解析 Token。
 * <p>
 * Header: X-User-Id (Long), X-User-Roles (comma-separated String)
 * <p>
 * 将用户身份写入请求属性，Controller 通过 {@code request.getAttribute("userId")} 获取。
 * 如需角色校验，在 Controller 方法上添加 {@link RequireRole} 注解。
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String ATTR_USER_ID = "currentUserId";
    public static final String ATTR_USER_ROLES = "currentUserRoles";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdHeader = request.getHeader(HEADER_USER_ID);
        String rolesHeader = request.getHeader(HEADER_USER_ROLES);

        if (userIdHeader != null) {
            request.setAttribute(ATTR_USER_ID, Long.parseLong(userIdHeader));
        }
        if (rolesHeader != null && !rolesHeader.isBlank()) {
            request.setAttribute(ATTR_USER_ROLES, rolesHeader.split(","));
        }
        return true;
    }

    /**
     * 从请求属性获取当前用户 ID，如果未认证则返回 null。
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute(ATTR_USER_ID);
    }

    /**
     * 从请求属性获取当前用户角色数组。
     */
    public static String[] getCurrentUserRoles(HttpServletRequest request) {
        return (String[]) request.getAttribute(ATTR_USER_ROLES);
    }
}
