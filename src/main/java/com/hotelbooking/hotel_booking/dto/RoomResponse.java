package com.hotelbooking.hotel_booking.dto;

import com.hotelbooking.hotel_booking.enums.RoomCategory;

import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        String roomNumber,
        RoomCategory category,
        Integer capacity,
        BigDecimal price,
        Long hotelId,
        String hotelName
) {
}
