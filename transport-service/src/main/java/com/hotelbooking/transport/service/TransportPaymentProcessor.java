package com.hotelbooking.transport.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class TransportPaymentProcessor {
    private static final Pattern VALID_TRANSACTION = Pattern.compile("^S [A-Za-z0-9]{8}$");

    public boolean isSuccessful(String transactionId) {
        return transactionId != null && VALID_TRANSACTION.matcher(transactionId).matches();
    }
}
