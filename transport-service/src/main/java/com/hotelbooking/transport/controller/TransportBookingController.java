package com.hotelbooking.transport.controller;

import com.hotelbooking.transport.dto.request.CreateTransportBookingRequest;
import com.hotelbooking.transport.dto.response.ApiResponse;
import com.hotelbooking.transport.dto.response.TransportBookingResponse;
import com.hotelbooking.transport.service.TransportBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class TransportBookingController {
    private final TransportBookingService bookingService;

    public TransportBookingController(TransportBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    ResponseEntity<ApiResponse<TransportBookingResponse>> createBooking(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            String authorizationHeader,
            @Valid @RequestBody CreateTransportBookingRequest request) {
        TransportBookingResponse booking =
                bookingService.createBooking(authorizationHeader, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Transport booking created; complete payment to confirm it", booking));
    }

    @GetMapping("/my")
    ApiResponse<List<TransportBookingResponse>> getMyBookings(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            String authorizationHeader) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Transport bookings retrieved",
                bookingService.getMyBookings(authorizationHeader));
    }
}
