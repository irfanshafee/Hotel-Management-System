package com.hotelbooking.hotel_booking.dto;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.RoomCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID bookingReference,
        BookingStatus status,
        LocalDate checkIn,
        LocalDate checkOut,
        LocalDateTime createdAt,
        String userName,
        String userEmail,
        Long hotelId,
        String hotelName,
        String city,
        Long roomId,
        String roomNumber,
        RoomCategory category,
        Integer capacity,
        BigDecimal price
) {
}
