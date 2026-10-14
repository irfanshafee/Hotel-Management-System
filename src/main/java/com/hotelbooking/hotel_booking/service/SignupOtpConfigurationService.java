package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.enums.ConfigurationKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.OptionalInt;

/** Signup OTP validity, read from configuration_table by its key. */
@Service
public class SignupOtpConfigurationService {
    private static final Logger log = LoggerFactory.getLogger(SignupOtpConfigurationService.class);

    private static final ConfigurationKey KEY = ConfigurationKey.SIGNUP_OTP_VALIDITY_MINUTES;
    static final Duration DEFAULT_OTP_VALIDITY = Duration.ofMinutes(KEY.defaultValue());

    private final ConfigurationService configuration;

    public SignupOtpConfigurationService(ConfigurationService configuration) {
        this.configuration = configuration;
    }

    /**
     * The validity stored under {@code signup.otp.validity.minutes}. A missing
     * row or invalid value never fails the request: it logs a warning and
     * returns {@link #DEFAULT_OTP_VALIDITY}. A database error is not caught.
     */
    public Duration getOtpValidity() {
        String value = configuration.getValue(KEY.key());
        if (value == null) {
            log.warn("Configuration {} is missing; using default {} minutes",
                    KEY.key(), KEY.defaultValue());
            return DEFAULT_OTP_VALIDITY;
        }

        OptionalInt minutes = KEY.parse(value);
        if (minutes.isEmpty()) {
            log.warn("Configuration {} has invalid value '{}' (expected {}-{}); using default {} minutes",
                    KEY.key(), value, KEY.minValue(), KEY.maxValue(), KEY.defaultValue());
            return DEFAULT_OTP_VALIDITY;
        }
        return Duration.ofMinutes(minutes.getAsInt());
    }
}
