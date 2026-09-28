package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.AuthResponse;
import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.LoginRequest;
import com.hotelbooking.hotel_booking.dto.RegisterRequest;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final ApiSuccessMessageCatalog successMessages;

    public AuthController(AuthService authService, ApiSuccessMessageCatalog successMessages) {
        this.authService = authService;
        this.successMessages = successMessages;
    }

    @PostMapping("/register")
    ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(HttpStatus.CREATED.value(),
                        successMessages.get("auth.registered"), response));
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(), successMessages.get("auth.login"),
                authService.login(request));
    }
}
