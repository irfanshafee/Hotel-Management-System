package com.hotelbooking.hotel_booking.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long roomId) {
        super("Room not found with id: " + roomId);
    }
}
