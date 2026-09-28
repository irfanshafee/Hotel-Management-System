package com.hotelbooking.hotel_booking.dto;

public record PaymentSubmissionResult(
        boolean successful,
        PaymentResponse payment
) {
}
