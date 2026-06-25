package com.rinko.infra.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * XSS 过滤器。
 * 对请求参数和 JSON Body 进行 HTML 转义，防止 XSS 攻击。
 */
public class XssFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            // For JSON bodies: cache the body, sanitize string values, re-wrap
            CachedBodyRequestWrapper cachedRequest = new CachedBodyRequestWrapper((HttpServletRequest) request);
            String rawBody = cachedRequest.getCachedBody();
            String sanitizedBody = sanitizeJsonStringValues(rawBody);
            cachedRequest.setBody(sanitizedBody.getBytes(StandardCharsets.UTF_8));
            chain.doFilter(cachedRequest, response);
        } else {
            // For form/query params: wrap as before
            chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * Sanitize HTML special characters in JSON string values only.
     * Matches text between JSON string quotes, handling escaped quotes.
     */
    static String sanitizeJsonStringValues(String json) {
        if (json == null || json.isEmpty()) return json;
        StringBuilder result = new StringBuilder(json.length());
        int i = 0;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '"') {
                // Start of a JSON string value
                int end = findStringEnd(json, i + 1);
                if (end > i) {
                    String value = json.substring(i + 1, end);
                    result.append('"').append(sanitize(value)).append('"');
                    i = end + 1;
                    continue;
                }
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }

    /**
     * Find the closing unescaped quote of a JSON string starting from position start.
     */
    private static int findStringEnd(String json, int start) {
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                return i;
            }
        }
        return -1; // unterminated string
    }

    private static String sanitize(String value) {
        if (value == null) return null;
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Wrapper for caching the request body (JSON) for re-reading after sanitization.
     */
    private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

        private byte[] body;

        CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.body = request.getInputStream().readAllBytes();
        }

        String getCachedBody() {
            return new String(body, StandardCharsets.UTF_8);
        }

        void setBody(byte[] newBody) {
            this.body = newBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return bais.read();
                }

                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Wrapper for sanitizing request parameters (form/query).
     */
    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return sanitize(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] sanitized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitized[i] = sanitize(values[i]);
            }
            return sanitized;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> map = new HashMap<>(super.getParameterMap());
            for (Map.Entry<String, String[]> entry : map.entrySet()) {
                String[] values = entry.getValue();
                String[] sanitized = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    sanitized[i] = sanitize(values[i]);
                }
                map.put(entry.getKey(), sanitized);
            }
            return map;
        }
    }
}
