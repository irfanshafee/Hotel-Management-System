package com.hotelbooking.transport.dto.response;

import com.hotelbooking.transport.enums.TripStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TripResponse(
        Long tripId,
        Long routeId,
        String origin,
        String destination,
        Long vehicleId,
        String busNumber,
        String vehicleName,
        Integer totalSeats,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime,
        BigDecimal fare,
        TripStatus status
) {
}
