package com.hotelbooking.hotel_booking.dto;

import java.time.LocalDateTime;

public record ConfigurationResponse(
        String key,
        String value,
        String description,
        LocalDateTime updatedAt
) {
}
