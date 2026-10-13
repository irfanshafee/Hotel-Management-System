package com.hotelbooking.transport.dto.response;

public record TransportPaymentSubmissionResult(
        boolean successful, TransportPaymentResponse payment) {}
