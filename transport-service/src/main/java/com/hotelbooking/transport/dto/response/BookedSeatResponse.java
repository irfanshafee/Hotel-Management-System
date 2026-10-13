package com.hotelbooking.transport.dto.response;

import java.math.BigDecimal;

public record BookedSeatResponse(
        Long seatId,
        String seatNumber,
        BigDecimal price
) {
}
