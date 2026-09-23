package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.CurrentUserResponse;
import com.hotelbooking.hotel_booking.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    CurrentUserResponse currentUser(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }
}
