package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.SignupOtpSettingsResponse;
import com.hotelbooking.hotel_booking.dto.UpdateSignupOtpSettingsRequest;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.SignupOtpSettingsAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin settings. NOTE: currently listed in security/open_apis.json, so these
 * endpoints need no login and anyone can call them. Intended for development
 * only; add an ADMIN role check before real users arrive.
 */
@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {
    private final SignupOtpSettingsAdminService settingsService;
    private final ApiSuccessMessageCatalog successMessages;

    public AdminSettingsController(SignupOtpSettingsAdminService settingsService,
                                   ApiSuccessMessageCatalog successMessages) {
        this.settingsService = settingsService;
        this.successMessages = successMessages;
    }

    @GetMapping("/signup-otp")
    ApiResponse<SignupOtpSettingsResponse> getSignupOtpSettings() {
        return new ApiResponse<>(HttpStatus.OK.value(),
                successMessages.get("admin.signup-otp.retrieved"),
                settingsService.getSettings());
    }

    @PutMapping("/signup-otp")
    ApiResponse<SignupOtpSettingsResponse> updateSignupOtpSettings(
            @Valid @RequestBody UpdateSignupOtpSettingsRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(),
                successMessages.get("admin.signup-otp.updated"),
                settingsService.updateValidityMinutes(request.validityMinutes()));
    }
}
