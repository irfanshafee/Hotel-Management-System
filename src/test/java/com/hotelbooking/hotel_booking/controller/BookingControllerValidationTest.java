package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.exception.ExceptionMessageCatalog;
import com.hotelbooking.hotel_booking.exception.GlobalExceptionHandler;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingControllerValidationTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        BookingController controller = new BookingController(
                org.mockito.Mockito.mock(BookingService.class),
                new ApiSuccessMessageCatalog(objectMapper));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(
                        new ExceptionMessageCatalog(objectMapper)))
                .build();
    }

    @Test
    void malformedUuidReturnsCleanBadRequest() throws Exception {
        mockMvc.perform(get("/api/bookings/not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(400))
                .andExpect(jsonPath("$.responseMessage")
                        .value("Invalid value for parameter: bookingId"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
