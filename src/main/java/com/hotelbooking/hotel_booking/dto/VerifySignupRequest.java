package com.hotelbooking.hotel_booking.dto;
import jakarta.validation.constraints.Email; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.Pattern;
public record VerifySignupRequest(@NotBlank @Email String email, @NotBlank @Pattern(regexp="\\d{6}") String otp) {}
