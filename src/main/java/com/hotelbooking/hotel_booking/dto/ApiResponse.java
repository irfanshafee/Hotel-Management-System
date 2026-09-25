package com.hotelbooking.hotel_booking.dto;

public record ApiResponse<T>(
        int responseCode,
        String responseMessage,
        T data
) {
}
