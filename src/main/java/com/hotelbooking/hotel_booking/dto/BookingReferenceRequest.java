package com.hotelbooking.hotel_booking.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookingReferenceRequest(
        @NotNull(message = "Booking reference is required")
        UUID bookingReference
) {
}
