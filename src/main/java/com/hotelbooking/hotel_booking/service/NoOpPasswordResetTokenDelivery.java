package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.entity.User;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Profile("!local")
@ConditionalOnProperty(name = "app.password-reset.delivery", havingValue = "noop", matchIfMissing = true)
class NoOpPasswordResetTokenDelivery implements PasswordResetTokenDelivery {
    @Override public void deliver(User user, String rawToken) { }
}
