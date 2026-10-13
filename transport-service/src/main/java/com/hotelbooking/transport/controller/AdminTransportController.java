package com.hotelbooking.transport.controller;

import com.hotelbooking.transport.dto.request.AdminCreateVehicleRequest;
import com.hotelbooking.transport.dto.request.AdminDriverRequest;
import com.hotelbooking.transport.dto.request.AdminRouteRequest;
import com.hotelbooking.transport.dto.request.AdminTripRequest;
import com.hotelbooking.transport.dto.request.AdminUpdateVehicleRequest;
import com.hotelbooking.transport.dto.response.AdminDriverResponse;
import com.hotelbooking.transport.dto.response.AdminRouteResponse;
import com.hotelbooking.transport.dto.response.AdminTripResponse;
import com.hotelbooking.transport.dto.response.AdminVehicleResponse;
import com.hotelbooking.transport.dto.response.ApiResponse;
import com.hotelbooking.transport.service.AdminTransportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminTransportController {
    private final AdminTransportService adminTransportService;

    public AdminTransportController(AdminTransportService adminTransportService) {
        this.adminTransportService = adminTransportService;
    }

    @GetMapping("/drivers")
    ApiResponse<List<AdminDriverResponse>> getDrivers(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ok("Drivers retrieved", adminTransportService.getDrivers(authorization));
    }

    @PostMapping("/drivers")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AdminDriverResponse> createDriver(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AdminDriverRequest request) {
        return created("Driver created", adminTransportService.createDriver(authorization, request));
    }

    @PutMapping("/drivers/{id}")
    ApiResponse<AdminDriverResponse> updateDriver(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody AdminDriverRequest request) {
        return ok("Driver updated", adminTransportService.updateDriver(authorization, id, request));
    }

    @GetMapping("/vehicles")
    ApiResponse<List<AdminVehicleResponse>> getVehicles(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ok("Vehicles retrieved", adminTransportService.getVehicles(authorization));
    }

    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AdminVehicleResponse> createVehicle(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AdminCreateVehicleRequest request) {
        return created("Vehicle and seats created",
                adminTransportService.createVehicle(authorization, request));
    }

    @PutMapping("/vehicles/{id}")
    ApiResponse<AdminVehicleResponse> updateVehicle(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateVehicleRequest request) {
        return ok("Vehicle updated", adminTransportService.updateVehicle(authorization, id, request));
    }

    @GetMapping("/routes")
    ApiResponse<List<AdminRouteResponse>> getRoutes(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ok("Routes retrieved", adminTransportService.getRoutes(authorization));
    }

    @PostMapping("/routes")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AdminRouteResponse> createRoute(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AdminRouteRequest request) {
        return created("Route created", adminTransportService.createRoute(authorization, request));
    }

    @PutMapping("/routes/{id}")
    ApiResponse<AdminRouteResponse> updateRoute(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody AdminRouteRequest request) {
        return ok("Route updated", adminTransportService.updateRoute(authorization, id, request));
    }

    @GetMapping("/trips")
    ApiResponse<List<AdminTripResponse>> getTrips(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ok("Trips retrieved", adminTransportService.getTrips(authorization));
    }

    @PostMapping("/trips")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AdminTripResponse> createTrip(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AdminTripRequest request) {
        return created("Trip created", adminTransportService.createTrip(authorization, request));
    }

    @PutMapping("/trips/{id}")
    ApiResponse<AdminTripResponse> updateTrip(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody AdminTripRequest request) {
        return ok("Trip updated", adminTransportService.updateTrip(authorization, id, request));
    }

    @PatchMapping("/trips/{id}/cancel")
    ApiResponse<AdminTripResponse> cancelTrip(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        return ok("Trip cancelled", adminTransportService.cancelTrip(authorization, id));
    }

    private <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), message, data);
    }

    private <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(HttpStatus.CREATED.value(), message, data);
    }
}
