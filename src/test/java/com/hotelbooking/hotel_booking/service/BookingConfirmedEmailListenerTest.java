package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.BookingConfirmationEmailData;
import com.hotelbooking.hotel_booking.enums.PaymentMethod;
import com.hotelbooking.hotel_booking.enums.RoomCategory;
import com.hotelbooking.hotel_booking.event.BookingConfirmedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BookingConfirmedEmailListenerTest {
    @Test
    void sendsConfirmationToBookingOwner() {
        BookingConfirmationEmailService emailService = mock(BookingConfirmationEmailService.class);
        BookingConfirmedEmailListener listener = new BookingConfirmedEmailListener(emailService);
        BookingConfirmedEvent event = event();

        listener.onBookingConfirmed(event);

        verify(emailService).sendBookingConfirmationEmail(event.emailData());
    }

    @Test
    void deliveryFailureDoesNotEscapeAfterCommitListener() {
        BookingConfirmationEmailService emailService = mock(BookingConfirmationEmailService.class);
        doThrow(new IllegalStateException("provider unavailable"))
                .when(emailService).sendBookingConfirmationEmail(org.mockito.ArgumentMatchers.any());

        assertDoesNotThrow(() -> new BookingConfirmedEmailListener(emailService).onBookingConfirmed(event()));
    }

    private BookingConfirmedEvent event() {
        return new BookingConfirmedEvent(new BookingConfirmationEmailData(
                "owner@example.com", "Owner", UUID.randomUUID(), "Test Hotel", "Dhaka", "Road 1", "101",
                RoomCategory.DELUXE, 2, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15), 5,
                new BigDecimal("15000.00"), PaymentMethod.CARD, "S 6789ABcd"));
    }
}
