package com.hotelbooking.hotel_booking.security;

import com.hotelbooking.hotel_booking.enums.UserRole;

public record AuthenticatedUser(Long id, String name, String email, UserRole role) {
    public static final String REQUEST_ATTRIBUTE = "authenticatedUser";
}
