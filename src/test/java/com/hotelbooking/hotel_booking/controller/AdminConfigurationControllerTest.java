package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ConfigurationResponse;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.exception.ExceptionMessageCatalog;
import com.hotelbooking.hotel_booking.exception.GlobalExceptionHandler;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.ConfigurationAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminConfigurationControllerTest {
    private static final String OTP_KEY = "signup.otp.validity.minutes";

    private final ConfigurationAdminService configurationService = mock(ConfigurationAdminService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        AdminConfigurationController controller = new AdminConfigurationController(
                configurationService, new ApiSuccessMessageCatalog(objectMapper));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(
                        new ExceptionMessageCatalog(objectMapper)))
                .build();
    }

    @Test
    void listsAllConfigurations() throws Exception {
        when(configurationService.getAll()).thenReturn(List.of(response("5")));

        mockMvc.perform(get("/api/admin/configurations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].key").value(OTP_KEY))
                .andExpect(jsonPath("$.data[0].value").value("5"));
    }

    @Test
    void getsOneConfigurationByDottedKey() throws Exception {
        when(configurationService.get(OTP_KEY)).thenReturn(response("5"));

        mockMvc.perform(get("/api/admin/configurations/" + OTP_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.key").value(OTP_KEY))
                .andExpect(jsonPath("$.data.value").value("5"));
    }

    @Test
    void unknownKeyReturnsNotFound() throws Exception {
        when(configurationService.get("missing.key"))
                .thenThrow(new ApiException("configuration.not-found", "missing.key"));

        mockMvc.perform(get("/api/admin/configurations/missing.key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseMessage")
                        .value("Configuration key not found: missing.key"));
    }

    @Test
    void updatesValue() throws Exception {
        when(configurationService.update(OTP_KEY, "10")).thenReturn(response("10"));

        mockMvc.perform(put("/api/admin/configurations/" + OTP_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseMessage").value("Configuration updated successfully"))
                .andExpect(jsonPath("$.data.value").value("10"));
    }

    @Test
    void invalidValueReturnsBadRequestWithRange() throws Exception {
        when(configurationService.update(OTP_KEY, "0"))
                .thenThrow(new ApiException("configuration.value.invalid", OTP_KEY, "1", "1440"));

        mockMvc.perform(put("/api/admin/configurations/" + OTP_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"0\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseMessage").value(
                        "Invalid value for signup.otp.validity.minutes: must be a whole number between 1 and 1440"));
    }

    @Test
    void missingValueReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/admin/configurations/" + OTP_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseMessage").value("value is required"));
        verify(configurationService, never()).update(anyString(), anyString());
    }

    private static ConfigurationResponse response(String value) {
        return new ConfigurationResponse(OTP_KEY, value, "description", LocalDateTime.now());
    }
}
