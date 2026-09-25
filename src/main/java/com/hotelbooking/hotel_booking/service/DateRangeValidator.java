package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.exception.InvalidFilterException;

import java.time.LocalDate;

final class DateRangeValidator {
    private DateRangeValidator() {
    }

    static void validate(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidFilterException("Check-in and check-out dates are required");
        }
        LocalDate today = LocalDate.now();
        if (checkIn.isBefore(today) || checkOut.isBefore(today)) {
            throw new InvalidFilterException("Check-in and check-out dates must not be in the past");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new InvalidFilterException("Check-in date must be before check-out date");
        }
    }
}
