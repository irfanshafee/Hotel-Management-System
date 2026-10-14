package com.hotelbooking.hotel_booking.dto;

import java.time.LocalDateTime;

/** {@code updatedAt} is null when no row exists yet and the default is in effect. */
public record SignupOtpSettingsResponse(
        int validityMinutes,
        LocalDateTime updatedAt
) {
}
