package com.hotelbooking.hotel_booking.service;
import org.springframework.stereotype.Service; import org.springframework.context.annotation.Profile; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Service @Profile("!local") @ConditionalOnProperty(name="app.password-reset.delivery",havingValue="noop",matchIfMissing=true)
class NoOpSignupOtpDelivery implements SignupOtpDelivery { public void deliver(String email,String name,String otp,int validityMinutes){} }
