package com.hotelbooking.hotel_booking.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyPaymentProcessorTest {
    private final DummyPaymentProcessor processor = new DummyPaymentProcessor();

    @ParameterizedTest
    @ValueSource(strings = {
            "S 6789ABcd",
            "S ABCD1234",
            "S 12345678",
            "S abcdefgh",
            "S a1B2c3D4"
    })
    void acceptsExactDummyTransactionFormat(String transactionId) {
        assertTrue(processor.isSuccessful(transactionId));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "",
            "ABC123",
            "S6789ABcd",
            "s 6789ABcd",
            "A 6789ABcd",
            "S 6789ABC",
            "S 6789ABcd9",
            "S 6789AB@d",
            "S  6789ABcd",
            " S 6789ABcd",
            "S 6789ABcd "
    })
    void rejectsAnythingOutsideExactDummyTransactionFormat(String transactionId) {
        assertFalse(processor.isSuccessful(transactionId));
    }
}
