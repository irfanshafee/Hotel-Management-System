package com.hotelbooking.hotel_booking.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PublicReferenceGenerationTest {

    @Test
    void bookingReferencesAreGeneratedAndUnique() {
        Booking first = new Booking();
        Booking second = new Booking();

        assertNotNull(first.getBookingReference());
        assertNotNull(second.getBookingReference());
        assertNotEquals(first.getBookingReference(), second.getBookingReference());
    }

    @Test
    void paymentReferencesAreGeneratedAndUnique() {
        Payment first = new Payment();
        Payment second = new Payment();

        assertNotNull(first.getPaymentReference());
        assertNotNull(second.getPaymentReference());
        assertNotEquals(first.getPaymentReference(), second.getPaymentReference());
    }
}
