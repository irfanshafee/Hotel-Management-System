package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.BookingResponse;
import com.hotelbooking.hotel_booking.dto.CreateBookingRequest;
import com.hotelbooking.hotel_booking.entity.Booking;
import com.hotelbooking.hotel_booking.entity.Hotel;
import com.hotelbooking.hotel_booking.entity.Room;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.RoomRepository;
import com.hotelbooking.hotel_booking.repository.UserRepository;
import com.hotelbooking.hotel_booking.security.AuthenticatedUserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private static final List<BookingStatus> BLOCKING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserContext authenticatedUserContext;

    public BookingService(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            UserRepository userRepository,
            AuthenticatedUserContext authenticatedUserContext) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        DateRangeValidator.validate(request.checkIn(), request.checkOut());
        User user = userRepository.findById(currentUserId())
                .orElseThrow(() -> new ApiException("invalid.credentials"));
        Room room = roomRepository.findByIdForBooking(request.roomId())
                .orElseThrow(() -> new ApiException("room.not.found", request.roomId()));

        long overlappingBookings = bookingRepository.countOverlappingActiveBookings(
                room.getId(), request.checkIn(), request.checkOut(), BLOCKING_STATUSES);
        if (overlappingBookings > 0) {
            throw new ApiException("room.unavailable");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartDate(request.checkIn());
        booking.setEndDate(request.checkOut());
        booking.setStatus(BookingStatus.PENDING);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        return bookingRepository.findByUserId(currentUserId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getMyBooking(UUID bookingUuid) {
        return toResponse(findOwnedBooking(bookingUuid));
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingUuid) {
        Booking booking = findOwnedBooking(bookingUuid);
        if (booking.getStatus() != BookingStatus.PENDING
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ApiException("booking.cancellation.invalid-status");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    private Booking findOwnedBooking(UUID bookingUuid) {
        return bookingRepository.findByBookingUuidAndUserId(
                        bookingUuid, currentUserId())
                .orElseThrow(() -> new ApiException("booking.not.found"));
    }

    private Long currentUserId() {
        return authenticatedUserContext.getRequiredUser().id();
    }

    private BookingResponse toResponse(Booking booking) {
        User user = booking.getUser();
        Room room = booking.getRoom();
        Hotel hotel = room.getHotel();
        return new BookingResponse(
                booking.getBookingUuid(), booking.getStatus(),
                booking.getStartDate(), booking.getEndDate(),
                booking.getCreatedAt(), user.getName(), user.getEmail(),
                hotel.getId(), hotel.getName(), hotel.getCity(), room.getId(),
                room.getRoomNumber(), room.getCategory(), room.getCapacity(), room.getPrice());
    }
}
