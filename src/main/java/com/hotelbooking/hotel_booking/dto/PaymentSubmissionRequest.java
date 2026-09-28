package com.hotelbooking.hotel_booking.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentSubmissionRequest(
        @NotNull(message = "Payment reference is required")
        UUID paymentReference,
        String transactionId) {
}
