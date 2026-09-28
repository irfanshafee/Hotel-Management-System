package com.hotelbooking.hotel_booking.security;

import com.hotelbooking.hotel_booking.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    @Test
    void protectedApiWithoutJwtReturnsStandardUnauthorizedResponse() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                mock(JwtService.class),
                mock(UserRepository.class),
                new ObjectMapper(),
                mock(OpenApiRegistry.class),
                mock(AuthenticatedUserContext.class));
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/bookings/details");
        request.setServletPath("/api/bookings/details");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"responseCode\":401"));
        assertTrue(response.getContentAsString().contains("Authentication required"));
        verify(chain, never()).doFilter(request, response);
    }
}
