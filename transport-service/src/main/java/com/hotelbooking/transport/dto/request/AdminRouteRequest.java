package com.hotelbooking.transport.dto.request;

import com.hotelbooking.transport.enums.RouteStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminRouteRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        @NotNull RouteStatus status
) {
}
