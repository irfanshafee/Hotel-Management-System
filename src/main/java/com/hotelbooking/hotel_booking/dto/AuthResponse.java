package com.hotelbooking.hotel_booking.dto;

import com.hotelbooking.hotel_booking.enums.UserRole;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        UserRole role
) {
}
