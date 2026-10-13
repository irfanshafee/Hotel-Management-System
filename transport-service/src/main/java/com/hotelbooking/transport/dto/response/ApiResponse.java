package com.hotelbooking.transport.dto.response;

public record ApiResponse<T>(
        int responseCode,
        String responseMessage,
        T data
) {
}
