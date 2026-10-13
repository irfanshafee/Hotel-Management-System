package com.hotelbooking.transport.dto.response;

import com.hotelbooking.transport.enums.TripStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AdminTripResponse(
        Long id,
        Long vehicleId,
        String busNumber,
        Long routeId,
        String origin,
        String destination,
        Long driverId,
        String driverName,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime,
        BigDecimal fare,
        TripStatus status
) {
}
