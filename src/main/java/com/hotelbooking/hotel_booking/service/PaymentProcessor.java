package com.hotelbooking.hotel_booking.service;

public interface PaymentProcessor {
    boolean isSuccessful(String transactionId);
}
