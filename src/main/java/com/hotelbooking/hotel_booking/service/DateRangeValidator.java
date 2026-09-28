package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.exception.ApiException;

import java.time.LocalDate;

final class DateRangeValidator {
    private DateRangeValidator() {
    }

    static void validate(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new ApiException("booking.dates.required");
        }
        LocalDate today = LocalDate.now();
        if (checkIn.isBefore(today) || checkOut.isBefore(today)) {
            throw new ApiException("booking.dates.in-past");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new ApiException("booking.date-range.invalid");
        }
    }
}
