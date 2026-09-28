package com.hotelbooking.hotel_booking.exception;

public class ApiException extends RuntimeException {
    private final String messageKey;
    private final Object[] messageArguments;

    public ApiException(String messageKey, Object... messageArguments) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArguments = messageArguments.clone();
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getMessageArguments() {
        return messageArguments.clone();
    }
}
