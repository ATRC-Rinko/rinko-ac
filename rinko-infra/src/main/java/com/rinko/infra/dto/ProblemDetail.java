package com.rinko.infra.dto;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RFC 7807 标准 Problem Detail 响应体。
 */
public class ProblemDetail {

    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    private final Instant timestamp;
    private final Map<String, Object> extensions;

    private ProblemDetail(Builder builder) {
        this.type = builder.type;
        this.title = builder.title;
        this.status = builder.status;
        this.detail = builder.detail;
        this.instance = builder.instance;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.extensions = new LinkedHashMap<>(builder.extensions);
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public String getInstance() {
        return instance;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public static Builder builder(String title, int status) {
        return new Builder(title, status);
    }

    public static class Builder {
        private String type = "about:blank";
        private final String title;
        private final int status;
        private String detail;
        private String instance;
        private Instant timestamp;
        private final Map<String, Object> extensions = new LinkedHashMap<>();

        private Builder(String title, int status) {
            this.title = title;
            this.status = status;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder extension(String key, Object value) {
            this.extensions.put(key, value);
            return this;
        }

        public ProblemDetail build() {
            return new ProblemDetail(this);
        }
    }
}
