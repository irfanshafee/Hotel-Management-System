package com.hotelbooking.transport.repository;

public interface SeatAvailabilityView {
    Long getSeatId();

    String getSeatNumber();

    boolean getAvailable();
}
