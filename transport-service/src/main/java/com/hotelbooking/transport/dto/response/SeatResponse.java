package com.hotelbooking.transport.dto.response;

import java.math.BigDecimal;

public record SeatResponse(
        Long seatId,
        String seatNumber,
        boolean available,
        BigDecimal price
) {
}
