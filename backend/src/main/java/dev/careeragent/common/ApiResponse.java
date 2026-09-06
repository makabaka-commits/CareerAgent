package dev.careeragent.common;

import java.time.Instant;

public record ApiResponse<T>(boolean success, T data, String message, Instant timestamp) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, data, null, Instant.now()); }
    public static ApiResponse<Void> ok() { return ok(null); }
    public static ApiResponse<Void> error(String message) { return new ApiResponse<>(false, null, message, Instant.now()); }
}

