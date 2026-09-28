package com.hotelbooking.hotel_booking.security;

import com.hotelbooking.hotel_booking.exception.ApiException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserContext {
    private final ThreadLocal<AuthenticatedUser> currentUser = new ThreadLocal<>();

    public void set(AuthenticatedUser authenticatedUser) {
        currentUser.set(authenticatedUser);
    }

    public AuthenticatedUser getRequiredUser() {
        AuthenticatedUser authenticatedUser = currentUser.get();
        if (authenticatedUser == null) {
            throw new ApiException("invalid.credentials");
        }
        return authenticatedUser;
    }

    public void clear() {
        currentUser.remove();
    }
}
