package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.ConfigurationResponse;
import com.hotelbooking.hotel_booking.dto.UpdateConfigurationRequest;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.ConfigurationAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin access to configuration_table. NOTE: currently listed in
 * security/open_apis.json, so these endpoints need no login and anyone can
 * call them. Intended for development only; add an ADMIN role check before
 * real users arrive.
 */
@RestController
@RequestMapping("/api/admin/configurations")
public class AdminConfigurationController {
    private final ConfigurationAdminService configurationService;
    private final ApiSuccessMessageCatalog successMessages;

    public AdminConfigurationController(ConfigurationAdminService configurationService,
                                        ApiSuccessMessageCatalog successMessages) {
        this.configurationService = configurationService;
        this.successMessages = successMessages;
    }

    @GetMapping
    ApiResponse<List<ConfigurationResponse>> getConfigurations() {
        return new ApiResponse<>(HttpStatus.OK.value(),
                successMessages.get("admin.configurations.retrieved"),
                configurationService.getAll());
    }

    @GetMapping("/{key}")
    ApiResponse<ConfigurationResponse> getConfiguration(@PathVariable String key) {
        return new ApiResponse<>(HttpStatus.OK.value(),
                successMessages.get("admin.configuration.retrieved"),
                configurationService.get(key));
    }

    @PutMapping("/{key}")
    ApiResponse<ConfigurationResponse> updateConfiguration(
            @PathVariable String key,
            @Valid @RequestBody UpdateConfigurationRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(),
                successMessages.get("admin.configuration.updated"),
                configurationService.update(key, request.value()));
    }
}
