package com.hotelbooking.transport.service;

import com.hotelbooking.transport.client.HotelUserClient;
import com.hotelbooking.transport.client.VerifiedUser;
import com.hotelbooking.transport.dto.response.CurrentUserResponse;
import com.hotelbooking.transport.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserIdentityService {
    private static final String BEARER_PREFIX = "Bearer ";

    private final HotelUserClient hotelUserClient;

    public UserIdentityService(HotelUserClient hotelUserClient) {
        this.hotelUserClient = hotelUserClient;
    }

    public CurrentUserResponse getCurrentUser(String authorizationHeader) {
        VerifiedUser user = verifyUser(authorizationHeader);
        return new CurrentUserResponse(
                user.id(), user.name(), user.email(), user.role());
    }

    public VerifiedUser verifyUser(String authorizationHeader) {
        validateAuthorizationHeader(authorizationHeader);
        return hotelUserClient.getCurrentUser(authorizationHeader.trim());
    }

    private void validateAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)
                || authorizationHeader.substring(BEARER_PREFIX.length()).isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Authentication required");
        }
    }
}
