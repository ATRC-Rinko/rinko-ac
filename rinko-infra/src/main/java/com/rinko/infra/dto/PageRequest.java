package com.rinko.infra.dto;

import java.util.Objects;

/**
 * 统一分页请求。
 */
public class PageRequest {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final int page;
    private final int size;
    private final SortOrder sort;

    public PageRequest() {
        this(DEFAULT_PAGE, DEFAULT_SIZE, null);
    }

    public PageRequest(int page, int size, SortOrder sort) {
        this.page = Math.max(1, page);
        this.size = Math.min(Math.max(1, size), MAX_SIZE);
        this.sort = sort;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public SortOrder getSort() {
        return sort;
    }

    public int getOffset() {
        return (page - 1) * size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PageRequest that)) {
            return false;
        }
        return page == that.page && size == that.size && Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, sort);
    }
}
