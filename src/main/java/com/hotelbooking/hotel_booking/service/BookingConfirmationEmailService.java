package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.BookingConfirmationEmailData;

public interface BookingConfirmationEmailService {
    void sendBookingConfirmationEmail(BookingConfirmationEmailData emailData);
}
