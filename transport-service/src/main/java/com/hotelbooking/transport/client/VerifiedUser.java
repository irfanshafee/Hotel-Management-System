package com.hotelbooking.transport.client;

public record VerifiedUser(
        Long id,
        String name,
        String email,
        String role
) {
}
