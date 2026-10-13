package com.hotelbooking.hotel_booking.dto;
import jakarta.validation.constraints.Email; import jakarta.validation.constraints.NotBlank;
public record ResendSignupOtpRequest(@NotBlank @Email String email) {}
