package com.hotelbooking.transport.controller;

import com.hotelbooking.transport.exception.GlobalExceptionHandler;
import com.hotelbooking.transport.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TripControllerValidationTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new TripController(mock(TripService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void missingSearchParametersReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(400));
    }

    @Test
    void malformedTripIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/trips/not-a-number/seats"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(400))
                .andExpect(jsonPath("$.responseMessage")
                        .value("Invalid value for parameter: tripId"));
    }
}
