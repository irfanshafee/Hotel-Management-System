package com.hotelbooking.transport.service;

import com.hotelbooking.transport.client.VerifiedUser;
import com.hotelbooking.transport.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthorizationService {
    private final UserIdentityService userIdentityService;

    public AdminAuthorizationService(UserIdentityService userIdentityService) {
        this.userIdentityService = userIdentityService;
    }

    public VerifiedUser requireAdmin(String authorizationHeader) {
        VerifiedUser user = userIdentityService.verifyUser(authorizationHeader);
        if (!"ADMIN".equalsIgnoreCase(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Administrator access is required");
        }
        return user;
    }
}
