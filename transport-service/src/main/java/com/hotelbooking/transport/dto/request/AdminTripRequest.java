package com.hotelbooking.transport.dto.request;

import com.hotelbooking.transport.enums.TripStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AdminTripRequest(
        @NotNull @Positive Long vehicleId,
        @NotNull @Positive Long routeId,
        @NotNull @Positive Long driverId,
        @NotNull @Future OffsetDateTime departureTime,
        @NotNull @Future OffsetDateTime arrivalTime,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal fare,
        @NotNull TripStatus status
) {
}
