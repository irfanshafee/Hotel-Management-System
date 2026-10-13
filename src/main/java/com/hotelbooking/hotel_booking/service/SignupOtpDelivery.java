package com.hotelbooking.hotel_booking.service;
public interface SignupOtpDelivery {
    void deliver(String email, String name, String otp, int validityMinutes);
}
