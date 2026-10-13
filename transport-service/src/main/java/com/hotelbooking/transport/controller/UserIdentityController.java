package com.hotelbooking.transport.controller;

import com.hotelbooking.transport.dto.response.ApiResponse;
import com.hotelbooking.transport.dto.response.CurrentUserResponse;
import com.hotelbooking.transport.service.UserIdentityService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserIdentityController {
    private final UserIdentityService userIdentityService;

    public UserIdentityController(UserIdentityService userIdentityService) {
        this.userIdentityService = userIdentityService;
    }

    @GetMapping("/me")
    ApiResponse<CurrentUserResponse> getCurrentUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            String authorizationHeader) {
        return new ApiResponse<>(HttpStatus.OK.value(), "User verified",
                userIdentityService.getCurrentUser(authorizationHeader));
    }
}
