package com.hotelbooking.transport.controller;

import com.hotelbooking.transport.dto.request.TransportPaymentSubmissionRequest;
import com.hotelbooking.transport.dto.response.ApiResponse;
import com.hotelbooking.transport.dto.response.TransportPaymentResponse;
import com.hotelbooking.transport.dto.response.TransportPaymentSubmissionResult;
import com.hotelbooking.transport.service.TransportPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class TransportPaymentController {
    private final TransportPaymentService paymentService;

    public TransportPaymentController(TransportPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate/{bookingId}")
    ApiResponse<TransportPaymentResponse> initiate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID bookingId) {
        return new ApiResponse<>(200, "Transport payment initiated",
                paymentService.initiate(authorization, bookingId));
    }

    @PostMapping("/{paymentId}/submit")
    ResponseEntity<ApiResponse<TransportPaymentResponse>> submit(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID paymentId,
            @Valid @RequestBody TransportPaymentSubmissionRequest request) {
        TransportPaymentSubmissionResult result = paymentService.submit(
                authorization, paymentId, request.transactionId());
        HttpStatus status = result.successful() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        String message = result.successful()
                ? "Payment successful. Transport booking confirmed."
                : "Payment failed. Transport booking cancelled.";
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), message, result.payment()));
    }

    @GetMapping("/booking/{bookingId}")
    ApiResponse<TransportPaymentResponse> getForBooking(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID bookingId) {
        return new ApiResponse<>(200, "Transport payment retrieved",
                paymentService.getForBooking(authorization, bookingId));
    }
}
