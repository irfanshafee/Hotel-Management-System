package com.hotelbooking.hotel_booking.exception;

public class BookingCancellationException extends RuntimeException {
    public BookingCancellationException() {
        super("Booking cannot be cancelled in its current status");
    }
}
