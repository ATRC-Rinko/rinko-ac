package com.rinko.infra.dto;

import java.time.LocalDateTime;

/**
 * 统一 API 响应包装类。
 *
 * @param <T> 响应数据类型
 */
public record ApiResponse<T>(int code, String message, T data, LocalDateTime timestamp) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "OK", data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "OK", null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }
}
