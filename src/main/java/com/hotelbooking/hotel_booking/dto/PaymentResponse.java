package com.hotelbooking.hotel_booking.dto;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.PaymentMethod;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID bookingId,
        String transactionId,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BookingStatus bookingStatus,
        LocalDateTime createdAt
) {
}
