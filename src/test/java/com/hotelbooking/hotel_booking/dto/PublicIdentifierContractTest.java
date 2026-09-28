package com.hotelbooking.hotel_booking.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicIdentifierContractTest {

    @Test
    void bookingResponseExposesReferenceInsteadOfInternalId() {
        Set<String> fields = componentNames(BookingResponse.class);

        assertTrue(fields.contains("bookingReference"));
        assertFalse(fields.contains("bookingId"));
        assertFalse(fields.contains("userId"));
    }

    @Test
    void paymentResponseExposesReferencesInsteadOfInternalIds() {
        Set<String> fields = componentNames(PaymentResponse.class);

        assertTrue(fields.contains("paymentReference"));
        assertTrue(fields.contains("bookingReference"));
        assertFalse(fields.contains("paymentId"));
        assertFalse(fields.contains("bookingId"));
    }

    private Set<String> componentNames(Class<?> recordType) {
        return Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());
    }
}
