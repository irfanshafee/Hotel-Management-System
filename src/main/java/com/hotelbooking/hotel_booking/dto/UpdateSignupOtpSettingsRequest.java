package com.hotelbooking.hotel_booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateSignupOtpSettingsRequest(
        @NotNull(message = "validityMinutes is required")
        @Min(value = 1, message = "validityMinutes must be between 1 and 1440")
        @Max(value = 1440, message = "validityMinutes must be between 1 and 1440")
        Integer validityMinutes
) {
}
