package com.hotelbooking.hotel_booking.service;
import org.springframework.stereotype.Service; import org.springframework.context.annotation.Profile; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.slf4j.*;
@Service @Profile("local") @ConditionalOnProperty(name="app.password-reset.delivery",havingValue="console",matchIfMissing=true)
class DevelopmentSignupOtpDelivery implements SignupOtpDelivery { private static final Logger LOG=LoggerFactory.getLogger(DevelopmentSignupOtpDelivery.class); public void deliver(String email,String name,String otp,int validityMinutes){LOG.info("Development signup OTP for {}: {} (valid for {} minutes)",email,otp,validityMinutes);} }
