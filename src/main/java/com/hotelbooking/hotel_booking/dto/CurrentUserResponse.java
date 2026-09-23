package com.hotelbooking.hotel_booking.dto;

import com.hotelbooking.hotel_booking.enums.UserRole;

public record CurrentUserResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {
}
