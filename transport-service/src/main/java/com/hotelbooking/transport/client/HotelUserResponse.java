package com.hotelbooking.transport.client;

record HotelUserResponse(
        Long id,
        String name,
        String email,
        String role
) {
}
