package com.hotelbooking.transport.dto.response;

import com.hotelbooking.transport.enums.TransportBookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TransportBookingResponse(
        UUID bookingReference,
        Long userId,
        Long tripId,
        String origin,
        String destination,
        String busNumber,
        OffsetDateTime departureTime,
        TransportBookingStatus status,
        BigDecimal totalAmount,
        OffsetDateTime bookedAt,
        List<BookedSeatResponse> seats
) {
}
