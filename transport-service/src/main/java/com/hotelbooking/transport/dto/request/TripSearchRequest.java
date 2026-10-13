package com.hotelbooking.transport.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TripSearchRequest(
        @NotBlank(message = "Origin is required")
        String origin,
        @NotBlank(message = "Destination is required")
        String destination,
        @NotNull(message = "Travel date is required")
        @FutureOrPresent(message = "Travel date must not be in the past")
        LocalDate date
) {
}
