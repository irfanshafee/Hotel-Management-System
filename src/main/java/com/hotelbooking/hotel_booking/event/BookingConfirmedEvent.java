package com.hotelbooking.hotel_booking.event;

import com.hotelbooking.hotel_booking.dto.BookingConfirmationEmailData;

/** Published only for the PENDING-to-CONFIRMED payment success transition. */
public record BookingConfirmedEvent(BookingConfirmationEmailData emailData) {
}
