package com.hotelbooking.hotel_booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBookingRequest(
        @NotNull(message = "Room ID is required")
        Long roomId,

        @NotNull(message = "Check-in date is required")
        LocalDate checkIn,

        @NotNull(message = "Check-out date is required")
        LocalDate checkOut
) {
}
