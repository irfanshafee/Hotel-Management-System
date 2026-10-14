package com.hotelbooking.hotel_booking.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Settings stored in "Production".configuration_table, with the rules a value
 * must satisfy. Code reads a setting only through its key here, never by a
 * hardcoded string elsewhere.
 */
public enum ConfigurationKey {
    SIGNUP_OTP_VALIDITY_MINUTES(
            "signup.otp.validity.minutes", 1, 1440, 5,
            "How long a signup verification code stays valid, in minutes (1-1440).");

    private final String key;
    private final int minValue;
    private final int maxValue;
    private final int defaultValue;
    private final String description;

    ConfigurationKey(String key, int minValue, int maxValue, int defaultValue, String description) {
        this.key = key;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public static Optional<ConfigurationKey> fromKey(String key) {
        return Arrays.stream(values()).filter(value -> value.key.equals(key)).findFirst();
    }

    /** The value as a whole number within range, or empty when it is not. */
    public OptionalInt parse(String value) {
        if (value == null) {
            return OptionalInt.empty();
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed >= minValue && parsed <= maxValue
                    ? OptionalInt.of(parsed) : OptionalInt.empty();
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public String key() {
        return key;
    }

    public int minValue() {
        return minValue;
    }

    public int maxValue() {
        return maxValue;
    }

    public int defaultValue() {
        return defaultValue;
    }

    public String description() {
        return description;
    }
}
