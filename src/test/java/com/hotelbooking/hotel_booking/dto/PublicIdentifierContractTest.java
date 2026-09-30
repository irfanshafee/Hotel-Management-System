package com.hotelbooking.hotel_booking.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PublicIdentifierContractTest {

    @Test
    void bookingResponseUsesUuidAsItsPublicId() {
        Map<String, Class<?>> fields = components(BookingResponse.class);

        assertEquals(UUID.class, fields.get("bookingId"));
    }

    @Test
    void paymentResponseUsesUuidsAsItsPublicIds() {
        Map<String, Class<?>> fields = components(PaymentResponse.class);

        assertEquals(UUID.class, fields.get("paymentId"));
        assertEquals(UUID.class, fields.get("bookingId"));
    }

    private Map<String, Class<?>> components(Class<?> recordType) {
        return Arrays.stream(recordType.getRecordComponents())
                .collect(Collectors.toMap(
                        RecordComponent::getName, RecordComponent::getType));
    }
}
