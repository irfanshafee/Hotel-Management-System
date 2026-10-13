package com.hotelbooking.transport.service;

import com.hotelbooking.transport.client.HotelUserClient;
import com.hotelbooking.transport.client.VerifiedUser;
import com.hotelbooking.transport.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserIdentityServiceTest {
    private HotelUserClient hotelUserClient;
    private UserIdentityService service;

    @BeforeEach
    void setUp() {
        hotelUserClient = mock(HotelUserClient.class);
        service = new UserIdentityService(hotelUserClient);
    }

    @Test
    void rejectsMissingAuthorizationHeaderWithoutCallingHotelService() {
        ApiException exception = assertThrows(
                ApiException.class, () -> service.getCurrentUser(null));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void returnsHotelVerifiedIdentity() {
        when(hotelUserClient.getCurrentUser("Bearer token"))
                .thenReturn(new VerifiedUser(
                        7L, "Test User", "user@example.com", "USER"));

        var response = service.getCurrentUser("Bearer token");

        verify(hotelUserClient).getCurrentUser("Bearer token");
        assertEquals(7L, response.id());
        assertEquals("user@example.com", response.email());
    }
}
