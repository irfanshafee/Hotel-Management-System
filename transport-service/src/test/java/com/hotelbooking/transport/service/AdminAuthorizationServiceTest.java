package com.hotelbooking.transport.service;

import com.hotelbooking.transport.client.VerifiedUser;
import com.hotelbooking.transport.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminAuthorizationServiceTest {
    private final UserIdentityService userIdentityService = mock(UserIdentityService.class);
    private final AdminAuthorizationService service =
            new AdminAuthorizationService(userIdentityService);

    @Test
    void acceptsAdminVerifiedByHotelService() {
        VerifiedUser admin = new VerifiedUser(1L, "Admin", "admin@example.com", "ADMIN");
        when(userIdentityService.verifyUser("Bearer admin-token")).thenReturn(admin);

        assertSame(admin, service.requireAdmin("Bearer admin-token"));
    }

    @Test
    void rejectsOrdinaryUser() {
        VerifiedUser user = new VerifiedUser(2L, "User", "user@example.com", "USER");
        when(userIdentityService.verifyUser("Bearer user-token")).thenReturn(user);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.requireAdmin("Bearer user-token"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }
}
