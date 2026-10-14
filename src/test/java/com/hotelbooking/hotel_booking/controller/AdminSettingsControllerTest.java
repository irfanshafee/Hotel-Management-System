package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.SignupOtpSettingsResponse;
import com.hotelbooking.hotel_booking.exception.ExceptionMessageCatalog;
import com.hotelbooking.hotel_booking.exception.GlobalExceptionHandler;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.SignupOtpSettingsAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminSettingsControllerTest {
    private final SignupOtpSettingsAdminService settingsService =
            mock(SignupOtpSettingsAdminService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        AdminSettingsController controller = new AdminSettingsController(
                settingsService, new ApiSuccessMessageCatalog(objectMapper));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(
                        new ExceptionMessageCatalog(objectMapper)))
                .build();
    }

    @Test
    void getReturnsCurrentSettings() throws Exception {
        when(settingsService.getSettings()).thenReturn(
                new SignupOtpSettingsResponse(5, LocalDateTime.of(2026, 10, 1, 12, 0)));

        mockMvc.perform(get("/api/admin/settings/signup-otp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value(200))
                .andExpect(jsonPath("$.data.validityMinutes").value(5));
    }

    @Test
    void putUpdatesValidity() throws Exception {
        when(settingsService.updateValidityMinutes(10)).thenReturn(
                new SignupOtpSettingsResponse(10, LocalDateTime.now()));

        mockMvc.perform(put("/api/admin/settings/signup-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"validityMinutes\": 10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseMessage")
                        .value("Signup OTP validity updated successfully"))
                .andExpect(jsonPath("$.data.validityMinutes").value(10));
        verify(settingsService).updateValidityMinutes(10);
    }

    @Test
    void putRejectsOutOfRangeValue() throws Exception {
        mockMvc.perform(put("/api/admin/settings/signup-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"validityMinutes\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseMessage")
                        .value("validityMinutes must be between 1 and 1440"));
        verify(settingsService, never()).updateValidityMinutes(anyInt());
    }

    @Test
    void putRejectsMissingValue() throws Exception {
        mockMvc.perform(put("/api/admin/settings/signup-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseMessage").value("validityMinutes is required"));
        verify(settingsService, never()).updateValidityMinutes(anyInt());
    }
}
