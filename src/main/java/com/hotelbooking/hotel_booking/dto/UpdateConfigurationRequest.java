package com.hotelbooking.hotel_booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateConfigurationRequest(
        @NotBlank(message = "value is required")
        @Size(max = 500, message = "value must not exceed 500 characters")
        String value
) {
}
