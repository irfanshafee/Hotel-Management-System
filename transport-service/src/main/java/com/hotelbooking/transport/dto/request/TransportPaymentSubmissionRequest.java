package com.hotelbooking.transport.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TransportPaymentSubmissionRequest(@NotBlank String transactionId) {}
