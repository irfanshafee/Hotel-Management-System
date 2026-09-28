package com.hotelbooking.hotel_booking.database;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicReferenceMigrationTest {

    @Test
    void migrationBackfillsBeforeEnforcingNotNullAndUniqueIndexes() throws Exception {
        String sql;
        try (var input = new ClassPathResource(
                "database/public-booking-payment-references.sql").getInputStream()) {
            sql = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .toLowerCase();
        }

        int bookingBackfill = sql.indexOf("update bookings");
        int bookingNotNull = sql.indexOf(
                "alter column booking_reference set not null");
        int paymentBackfill = sql.indexOf("update payments");
        int paymentNotNull = sql.indexOf(
                "alter column payment_reference set not null");

        assertTrue(bookingBackfill >= 0 && bookingBackfill < bookingNotNull);
        assertTrue(paymentBackfill >= 0 && paymentBackfill < paymentNotNull);
        assertTrue(sql.contains("unique index if not exists uk_bookings_booking_reference"));
        assertTrue(sql.contains("unique index if not exists uk_payments_payment_reference"));
    }
}
