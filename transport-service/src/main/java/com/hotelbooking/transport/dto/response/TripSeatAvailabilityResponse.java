package com.hotelbooking.transport.dto.response;

import java.util.List;

public record TripSeatAvailabilityResponse(
        Long tripId,
        Long vehicleId,
        String busNumber,
        String vehicleName,
        List<SeatResponse> seats
) {
}
