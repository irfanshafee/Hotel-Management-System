package com.hotelbooking.transport.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AdminCreateVehicleRequest(
        @NotBlank String busNumber,
        @NotBlank String name,
        @Min(value = 1, message = "Total seats must be positive")
        @Max(value = 60, message = "Total seats must not exceed 60")
        int totalSeats
) {
}
