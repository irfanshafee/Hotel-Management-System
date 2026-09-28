package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.BookingReferenceRequest;
import com.hotelbooking.hotel_booking.dto.PaymentResponse;
import com.hotelbooking.hotel_booking.dto.PaymentSubmissionRequest;
import com.hotelbooking.hotel_booking.dto.PaymentSubmissionResult;
import com.hotelbooking.hotel_booking.exception.ExceptionMessageCatalog;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final ApiSuccessMessageCatalog successMessages;
    private final ExceptionMessageCatalog exceptionMessages;

    public PaymentController(
            PaymentService paymentService,
            ApiSuccessMessageCatalog successMessages,
            ExceptionMessageCatalog exceptionMessages) {
        this.paymentService = paymentService;
        this.successMessages = successMessages;
        this.exceptionMessages = exceptionMessages;
    }

    @PostMapping("/initiate")
    ApiResponse<PaymentResponse> initiatePayment(
            @Valid @RequestBody BookingReferenceRequest request) {
        PaymentResponse payment = paymentService.initiatePayment(request.bookingReference());
        return new ApiResponse<>(
                HttpStatus.OK.value(), successMessages.get("payment.initiated"), payment);
    }

    @PostMapping("/submit")
    ResponseEntity<ApiResponse<PaymentResponse>> submitPayment(
            @Valid @RequestBody PaymentSubmissionRequest submission) {
        PaymentSubmissionResult result = paymentService.submitPayment(
                submission.paymentReference(), submission.transactionId());

        HttpStatus status = result.successful() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        String message = result.successful()
                ? successMessages.get("payment.successful")
                : exceptionMessages.resolve("payment.failed.invalid-transaction").message();
        return ResponseEntity.status(status).body(
                new ApiResponse<>(status.value(), message, result.payment()));
    }

    @PostMapping("/booking")
    ApiResponse<PaymentResponse> getPaymentForBooking(
            @Valid @RequestBody BookingReferenceRequest request) {
        PaymentResponse payment = paymentService.getPaymentForBooking(
                request.bookingReference());
        return new ApiResponse<>(
                HttpStatus.OK.value(), successMessages.get("payment.retrieved"), payment);
    }
}
