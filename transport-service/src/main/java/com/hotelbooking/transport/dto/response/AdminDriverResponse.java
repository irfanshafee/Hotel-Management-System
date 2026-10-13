package com.hotelbooking.transport.dto.response;

import com.hotelbooking.transport.enums.DriverStatus;

public record AdminDriverResponse(
        Long id, String name, String phoneNumber,
        String nidNumber, DriverStatus status
) {
}
