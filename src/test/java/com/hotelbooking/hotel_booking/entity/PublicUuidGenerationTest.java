package com.hotelbooking.hotel_booking.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PublicUuidGenerationTest {

    @Test
    void bookingUuidsAreGeneratedAndUnique() {
        Booking first = new Booking();
        Booking second = new Booking();

        assertNotNull(first.getBookingUuid());
        assertNotNull(second.getBookingUuid());
        assertNotEquals(first.getBookingUuid(), second.getBookingUuid());
    }

    @Test
    void paymentUuidsAreGeneratedAndUnique() {
        Payment first = new Payment();
        Payment second = new Payment();

        assertNotNull(first.getPaymentUuid());
        assertNotNull(second.getPaymentUuid());
        assertNotEquals(first.getPaymentUuid(), second.getPaymentUuid());
    }
}
