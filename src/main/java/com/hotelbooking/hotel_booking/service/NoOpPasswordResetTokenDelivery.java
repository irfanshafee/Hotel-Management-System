package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.entity.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!local")
class NoOpPasswordResetTokenDelivery implements PasswordResetTokenDelivery {
    @Override public void deliver(User user, String rawToken) { }
}
