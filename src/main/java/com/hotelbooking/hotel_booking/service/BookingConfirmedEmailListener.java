package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.event.BookingConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Sends only after the payment and booking status changes have committed. */
@Component
@ConditionalOnProperty(name = "app.password-reset.delivery", havingValue = "resend")
class BookingConfirmedEmailListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookingConfirmedEmailListener.class);
    private final BookingConfirmationEmailService emailService;

    BookingConfirmedEmailListener(BookingConfirmationEmailService emailService) {
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        try {
            emailService.sendBookingConfirmationEmail(event.emailData());
        } catch (RuntimeException exception) {
            LOGGER.error("Booking confirmation email delivery failed after booking confirmation", exception);
        }
    }
}
