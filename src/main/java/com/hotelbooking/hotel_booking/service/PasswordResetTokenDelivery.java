package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.entity.User;

public interface PasswordResetTokenDelivery {
    void deliver(User user, String rawToken);
}
