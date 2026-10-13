package com.hotelbooking.transport.dto.request;

import com.hotelbooking.transport.enums.DriverStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminDriverRequest(
        @NotBlank String name,
        @NotBlank String phoneNumber,
        @NotBlank String nidNumber,
        @NotNull DriverStatus status
) {
}
