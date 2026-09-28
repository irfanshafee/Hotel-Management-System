package com.hotelbooking.hotel_booking.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DummyPaymentProcessor implements PaymentProcessor {
    public static final String TRANSACTION_ID_REGEX = "^S [A-Za-z0-9]{8}$";
    private static final Pattern TRANSACTION_ID_PATTERN =
            Pattern.compile(TRANSACTION_ID_REGEX);

    @Override
    public boolean isSuccessful(String transactionId) {
        return transactionId != null
                && TRANSACTION_ID_PATTERN.matcher(transactionId).matches();
    }
}
