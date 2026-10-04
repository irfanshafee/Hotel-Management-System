package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
class DevelopmentPasswordResetTokenDelivery implements PasswordResetTokenDelivery {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevelopmentPasswordResetTokenDelivery.class);

    @Override
    public void deliver(User user, String rawToken) {
        LOGGER.info("Development password reset token for {}: {}", user.getEmail(), rawToken);
    }
}
