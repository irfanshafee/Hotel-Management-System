package com.hotelbooking.transport.dto.response;

import com.hotelbooking.transport.enums.RouteStatus;

public record AdminRouteResponse(
        Long id, String origin, String destination, RouteStatus status
) {
}
