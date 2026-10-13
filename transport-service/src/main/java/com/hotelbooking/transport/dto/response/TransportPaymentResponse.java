package com.hotelbooking.transport.dto.response;

import com.hotelbooking.transport.enums.TransportBookingStatus;
import com.hotelbooking.transport.enums.TransportPaymentMethod;
import com.hotelbooking.transport.enums.TransportPaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransportPaymentResponse(
        UUID paymentId, UUID bookingId, String transactionId,
        BigDecimal amount, BigDecimal paidAmount, BigDecimal remainingAmount,
        TransportPaymentMethod paymentMethod, TransportPaymentStatus paymentStatus,
        TransportBookingStatus bookingStatus, OffsetDateTime createdAt) {}
