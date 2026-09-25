package com.hotelbooking.hotel_booking.exception;

public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException(Long hotelId) {
        super("Hotel not found with id: " + hotelId);
    }
}
