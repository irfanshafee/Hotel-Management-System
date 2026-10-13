package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.AuthResponse;
import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.LoginRequest;
import com.hotelbooking.hotel_booking.dto.ForgotPasswordRequest;
import com.hotelbooking.hotel_booking.dto.RegisterRequest;
import com.hotelbooking.hotel_booking.dto.ResetPasswordRequest;
import com.hotelbooking.hotel_booking.dto.VerifySignupRequest;
import com.hotelbooking.hotel_booking.dto.ResendSignupOtpRequest;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.AuthService;
import com.hotelbooking.hotel_booking.service.PasswordResetService;
import com.hotelbooking.hotel_booking.service.SignupVerificationService;
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
    private final SignupVerificationService signupVerificationService;

    public AuthController(AuthService authService, ApiSuccessMessageCatalog successMessages,
                          PasswordResetService passwordResetService,
                          SignupVerificationService signupVerificationService) {
        this.authService = authService;
        this.successMessages = successMessages;
        this.passwordResetService = passwordResetService;
        this.signupVerificationService = signupVerificationService;
    }

    @PostMapping("/register")
    ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        signupVerificationService.start(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), successMessages.get("auth.signup.otp.sent"), null));
    }

    @PostMapping("/verify-signup")
    ApiResponse<AuthResponse> verifySignup(@Valid @RequestBody VerifySignupRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(), successMessages.get("auth.signup.verified"), signupVerificationService.verify(request));
    }

    @PostMapping("/resend-signup-otp")
    ApiResponse<Void> resendSignupOtp(@Valid @RequestBody ResendSignupOtpRequest request) {
        signupVerificationService.resend(request);
        return new ApiResponse<>(HttpStatus.OK.value(), successMessages.get("auth.signup.otp.sent"), null);
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
