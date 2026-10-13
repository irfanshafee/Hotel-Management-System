package com.hotelbooking.transport.dto.request;

import com.hotelbooking.transport.enums.VehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminUpdateVehicleRequest(
        @NotBlank String name,
        @NotNull VehicleStatus status
) {
}
