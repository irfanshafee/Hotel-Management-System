package com.hotelbooking.transport.service;

import com.hotelbooking.transport.dto.request.AdminCreateVehicleRequest;
import com.hotelbooking.transport.dto.request.AdminDriverRequest;
import com.hotelbooking.transport.dto.request.AdminRouteRequest;
import com.hotelbooking.transport.dto.request.AdminTripRequest;
import com.hotelbooking.transport.dto.request.AdminUpdateVehicleRequest;
import com.hotelbooking.transport.dto.response.AdminDriverResponse;
import com.hotelbooking.transport.dto.response.AdminRouteResponse;
import com.hotelbooking.transport.dto.response.AdminTripResponse;
import com.hotelbooking.transport.dto.response.AdminVehicleResponse;
import com.hotelbooking.transport.entity.Driver;
import com.hotelbooking.transport.entity.Route;
import com.hotelbooking.transport.entity.Seat;
import com.hotelbooking.transport.entity.Trip;
import com.hotelbooking.transport.entity.Vehicle;
import com.hotelbooking.transport.enums.DriverStatus;
import com.hotelbooking.transport.enums.RouteStatus;
import com.hotelbooking.transport.enums.TripStatus;
import com.hotelbooking.transport.enums.VehicleStatus;
import com.hotelbooking.transport.exception.ApiException;
import com.hotelbooking.transport.repository.DriverRepository;
import com.hotelbooking.transport.repository.RouteRepository;
import com.hotelbooking.transport.repository.SeatRepository;
import com.hotelbooking.transport.repository.TransportBookingRepository;
import com.hotelbooking.transport.repository.TripRepository;
import com.hotelbooking.transport.repository.VehicleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminTransportService {
    private final AdminAuthorizationService authorizationService;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final TransportBookingRepository bookingRepository;

    public AdminTransportService(
            AdminAuthorizationService authorizationService,
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository,
            RouteRepository routeRepository,
            TripRepository tripRepository,
            SeatRepository seatRepository,
            TransportBookingRepository bookingRepository) {
        this.authorizationService = authorizationService;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminDriverResponse> getDrivers(String header) {
        authorizationService.requireAdmin(header);
        return driverRepository.findAll(Sort.by("id")).stream().map(this::driverResponse).toList();
    }

    @Transactional
    public AdminDriverResponse createDriver(String header, AdminDriverRequest request) {
        authorizationService.requireAdmin(header);
        Driver driver = new Driver(trim(request.name()), trim(request.phoneNumber()),
                trim(request.nidNumber()), request.status());
        return driverResponse(driverRepository.save(driver));
    }

    @Transactional
    public AdminDriverResponse updateDriver(
            String header, Long id, AdminDriverRequest request) {
        authorizationService.requireAdmin(header);
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> notFound("Driver", id));
        driver.update(trim(request.name()), trim(request.phoneNumber()),
                trim(request.nidNumber()), request.status());
        return driverResponse(driver);
    }

    @Transactional(readOnly = true)
    public List<AdminVehicleResponse> getVehicles(String header) {
        authorizationService.requireAdmin(header);
        return vehicleRepository.findAll(Sort.by("id")).stream().map(this::vehicleResponse).toList();
    }

    @Transactional
    public AdminVehicleResponse createVehicle(
            String header, AdminCreateVehicleRequest request) {
        authorizationService.requireAdmin(header);
        Vehicle vehicle = vehicleRepository.save(new Vehicle(
                trim(request.busNumber()), trim(request.name()),
                request.totalSeats(), VehicleStatus.ACTIVE));
        List<Seat> seats = new ArrayList<>();
        for (int position = 1; position <= request.totalSeats(); position++) {
            seats.add(new Seat(vehicle, seatLabel(position)));
        }
        seatRepository.saveAll(seats);
        return vehicleResponse(vehicle);
    }

    @Transactional
    public AdminVehicleResponse updateVehicle(
            String header, Long id, AdminUpdateVehicleRequest request) {
        authorizationService.requireAdmin(header);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> notFound("Vehicle", id));
        vehicle.update(trim(request.name()), request.status());
        return vehicleResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public List<AdminRouteResponse> getRoutes(String header) {
        authorizationService.requireAdmin(header);
        return routeRepository.findAll(Sort.by("id")).stream().map(this::routeResponse).toList();
    }

    @Transactional
    public AdminRouteResponse createRoute(String header, AdminRouteRequest request) {
        authorizationService.requireAdmin(header);
        validateRoute(request.origin(), request.destination());
        Route route = new Route(trim(request.origin()), trim(request.destination()), request.status());
        return routeResponse(routeRepository.save(route));
    }

    @Transactional
    public AdminRouteResponse updateRoute(
            String header, Long id, AdminRouteRequest request) {
        authorizationService.requireAdmin(header);
        validateRoute(request.origin(), request.destination());
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> notFound("Route", id));
        route.update(trim(request.origin()), trim(request.destination()), request.status());
        return routeResponse(route);
    }

    @Transactional(readOnly = true)
    public List<AdminTripResponse> getTrips(String header) {
        authorizationService.requireAdmin(header);
        return tripRepository.findAll(Sort.by("departureTime")).stream()
                .map(this::tripResponse).toList();
    }

    @Transactional
    public AdminTripResponse createTrip(String header, AdminTripRequest request) {
        authorizationService.requireAdmin(header);
        TripResources resources = validateTripRequest(null, request);
        Trip trip = new Trip(resources.vehicle(), resources.route(), resources.driver(),
                request.departureTime(), request.arrivalTime(), request.fare(), request.status());
        return tripResponse(tripRepository.save(trip));
    }

    @Transactional
    public AdminTripResponse updateTrip(
            String header, Long id, AdminTripRequest request) {
        authorizationService.requireAdmin(header);
        Trip trip = tripRepository.findDetailedById(id)
                .orElseThrow(() -> notFound("Trip", id));
        ensureTripHasNoBookings(id);
        TripResources resources = validateTripRequest(id, request);
        trip.update(resources.vehicle(), resources.route(), resources.driver(),
                request.departureTime(), request.arrivalTime(), request.fare(), request.status());
        return tripResponse(trip);
    }

    @Transactional
    public AdminTripResponse cancelTrip(String header, Long id) {
        authorizationService.requireAdmin(header);
        Trip trip = tripRepository.findDetailedById(id)
                .orElseThrow(() -> notFound("Trip", id));
        ensureTripHasNoBookings(id);
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Only a scheduled trip may be cancelled");
        }
        trip.cancel();
        return tripResponse(trip);
    }

    private TripResources validateTripRequest(Long excludeTripId, AdminTripRequest request) {
        if (request.status() != TripStatus.SCHEDULED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "New and edited trips must be SCHEDULED; use the cancellation endpoint to cancel a trip");
        }
        if (!request.arrivalTime().isAfter(request.departureTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Arrival time must be after departure time");
        }
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> notFound("Vehicle", request.vehicleId()));
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> notFound("Route", request.routeId()));
        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> notFound("Driver", request.driverId()));
        if (request.status() == TripStatus.SCHEDULED
                && (vehicle.getStatus() != VehicleStatus.ACTIVE
                || route.getStatus() != RouteStatus.ACTIVE
                || driver.getStatus() != DriverStatus.ACTIVE)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Scheduled trips require an active vehicle, route, and driver");
        }
        long conflicts = tripRepository.countResourceConflicts(
                excludeTripId, vehicle.getId(), driver.getId(),
                request.departureTime(), request.arrivalTime());
        if (conflicts > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "The vehicle or driver is already assigned during this time");
        }
        return new TripResources(vehicle, route, driver);
    }

    private void ensureTripHasNoBookings(Long tripId) {
        if (bookingRepository.existsByTripId(tripId)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Trips with bookings cannot be edited or cancelled");
        }
    }

    private void validateRoute(String origin, String destination) {
        if (trim(origin).equalsIgnoreCase(trim(destination))) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Route origin and destination must be different");
        }
    }

    private String seatLabel(int position) {
        char column = (char) ('A' + ((position - 1) % 4));
        int row = ((position - 1) / 4) + 1;
        return column + Integer.toString(row);
    }

    private String trim(String value) {
        return value.trim();
    }

    private ApiException notFound(String resource, Long id) {
        return new ApiException(HttpStatus.NOT_FOUND,
                resource + " not found with id: " + id);
    }

    private AdminDriverResponse driverResponse(Driver driver) {
        return new AdminDriverResponse(driver.getId(), driver.getName(),
                driver.getPhoneNumber(), driver.getNidNumber(), driver.getStatus());
    }

    private AdminVehicleResponse vehicleResponse(Vehicle vehicle) {
        return new AdminVehicleResponse(vehicle.getId(), vehicle.getBusNumber(),
                vehicle.getName(), vehicle.getTotalSeats(), vehicle.getStatus());
    }

    private AdminRouteResponse routeResponse(Route route) {
        return new AdminRouteResponse(route.getId(), route.getOrigin(),
                route.getDestination(), route.getStatus());
    }

    private AdminTripResponse tripResponse(Trip trip) {
        return new AdminTripResponse(trip.getId(), trip.getVehicle().getId(),
                trip.getVehicle().getBusNumber(), trip.getRoute().getId(),
                trip.getRoute().getOrigin(), trip.getRoute().getDestination(),
                trip.getDriver().getId(), trip.getDriver().getName(),
                trip.getDepartureTime(), trip.getArrivalTime(),
                trip.getFare(), trip.getStatus());
    }

    private record TripResources(Vehicle vehicle, Route route, Driver driver) {
    }
}
