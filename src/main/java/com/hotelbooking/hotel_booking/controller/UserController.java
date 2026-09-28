package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.CurrentUserResponse;
import com.hotelbooking.hotel_booking.security.AuthenticatedUser;
import com.hotelbooking.hotel_booking.security.AuthenticatedUserContext;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final ApiSuccessMessageCatalog successMessages;
    private final AuthenticatedUserContext authenticatedUserContext;

    public UserController(
            ApiSuccessMessageCatalog successMessages,
            AuthenticatedUserContext authenticatedUserContext) {
        this.successMessages = successMessages;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping("/me")
    ApiResponse<CurrentUserResponse> currentUser() {
        AuthenticatedUser user = authenticatedUserContext.getRequiredUser();
        CurrentUserResponse response = new CurrentUserResponse(
                user.id(), user.name(), user.email(), user.role());
        return new ApiResponse<>(HttpStatus.OK.value(),
                successMessages.get("user.retrieved"), response);
    }
}
