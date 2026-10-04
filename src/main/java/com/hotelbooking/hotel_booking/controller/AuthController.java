package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.AuthResponse;
import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.LoginRequest;
import com.hotelbooking.hotel_booking.dto.ForgotPasswordRequest;
import com.hotelbooking.hotel_booking.dto.RegisterRequest;
import com.hotelbooking.hotel_booking.dto.ResetPasswordRequest;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.AuthService;
import com.hotelbooking.hotel_booking.service.PasswordResetService;
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
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, ApiSuccessMessageCatalog successMessages,
                          PasswordResetService passwordResetService) {
        this.authService = authService;
        this.successMessages = successMessages;
        this.passwordResetService = passwordResetService;
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

    @PostMapping("/forgot-password")
    ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        return new ApiResponse<>(HttpStatus.OK.value(),
                successMessages.get("auth.password-reset.requested"), null);
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return new ApiResponse<>(HttpStatus.OK.value(),
                successMessages.get("auth.password-reset.successful"), null);
    }
}
