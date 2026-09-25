package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.CurrentUserResponse;
import com.hotelbooking.hotel_booking.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @GetMapping("/me")
    ApiResponse<CurrentUserResponse> currentUser(HttpServletRequest request) {
        AuthenticatedUser user = (AuthenticatedUser) request.getAttribute(
                AuthenticatedUser.REQUEST_ATTRIBUTE);
        CurrentUserResponse response = new CurrentUserResponse(
                user.id(), user.name(), user.email(), user.role());
        return new ApiResponse<>(HttpStatus.OK.value(),
                "User retrieved successfully", response);
    }
}
