package com.hotelbooking.hotel_booking.dto;

public record HotelResponse(
        Long id,
        String name,
        String city,
        String address,
        String description
) {
}
