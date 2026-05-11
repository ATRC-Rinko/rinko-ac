package com.rinko.infra.dto;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页响应。
 *
 * @param <T> 内容类型
 */
public record PageResponse<T>(List<T> content, long totalElements, int page, int size) {

    public PageResponse {
        content = Collections.unmodifiableList(content);
    }

    public int totalPages() {
        return size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public boolean isFirst() {
        return page <= 1;
    }

    public boolean isLast() {
        return page >= totalPages();
    }
}
