package com.hotelbooking.transport.dto.response;

public record CurrentUserResponse(
        Long id,
        String name,
        String email,
        String role
) {
}
