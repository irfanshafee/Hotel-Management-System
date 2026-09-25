package com.hotelbooking.hotel_booking.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long bookingId) {
        super("Booking not found with id: " + bookingId);
    }
}
