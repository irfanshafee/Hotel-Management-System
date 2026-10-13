package com.hotelbooking.transport.dto.response;

import com.hotelbooking.transport.enums.VehicleStatus;

public record AdminVehicleResponse(
        Long id, String busNumber, String name,
        Integer totalSeats, VehicleStatus status
) {
}
