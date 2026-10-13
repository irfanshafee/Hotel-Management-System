package com.hotelbooking.transport.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateTransportBookingRequest(
        @NotNull(message = "Trip id is required")
        @Positive(message = "Trip id must be positive")
        Long tripId,
        @NotEmpty(message = "At least one seat is required")
        @Size(max = 10, message = "A maximum of 10 seats may be booked at once")
        List<@NotNull(message = "Seat id is required")
                @Positive(message = "Seat id must be positive") Long> seatIds
) {
}
