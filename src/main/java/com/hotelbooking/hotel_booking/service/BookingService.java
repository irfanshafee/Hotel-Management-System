package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.BookingResponse;
import com.hotelbooking.hotel_booking.dto.CreateBookingRequest;
import com.hotelbooking.hotel_booking.entity.Booking;
import com.hotelbooking.hotel_booking.entity.Hotel;
import com.hotelbooking.hotel_booking.entity.Room;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.exception.BookingCancellationException;
import com.hotelbooking.hotel_booking.exception.BookingNotFoundException;
import com.hotelbooking.hotel_booking.exception.InvalidCredentialsException;
import com.hotelbooking.hotel_booking.exception.RoomNotFoundException;
import com.hotelbooking.hotel_booking.exception.RoomUnavailableException;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.RoomRepository;
import com.hotelbooking.hotel_booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {
    private static final List<BookingStatus> BLOCKING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse createBooking(Long authenticatedUserId, CreateBookingRequest request) {
        DateRangeValidator.validate(request.checkIn(), request.checkOut());
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(InvalidCredentialsException::new);
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new RoomNotFoundException(request.roomId()));

        long overlappingBookings = bookingRepository.countOverlappingActiveBookings(
                room.getId(), request.checkIn(), request.checkOut(), BLOCKING_STATUSES);
        if (overlappingBookings > 0) {
            throw new RoomUnavailableException();
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
    public List<BookingResponse> getMyBookings(Long authenticatedUserId) {
        return bookingRepository.findByUserId(authenticatedUserId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getMyBooking(Long authenticatedUserId, Long bookingId) {
        return toResponse(findOwnedBooking(authenticatedUserId, bookingId));
    }

    @Transactional
    public BookingResponse cancelBooking(Long authenticatedUserId, Long bookingId) {
        Booking booking = findOwnedBooking(authenticatedUserId, bookingId);
        if (booking.getStatus() != BookingStatus.PENDING
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingCancellationException();
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    private Booking findOwnedBooking(Long authenticatedUserId, Long bookingId) {
        return bookingRepository.findByIdAndUserId(bookingId, authenticatedUserId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    private BookingResponse toResponse(Booking booking) {
        User user = booking.getUser();
        Room room = booking.getRoom();
        Hotel hotel = room.getHotel();
        return new BookingResponse(
                booking.getId(), booking.getStatus(), booking.getStartDate(), booking.getEndDate(),
                booking.getCreatedAt(), user.getId(), user.getName(), user.getEmail(),
                hotel.getId(), hotel.getName(), hotel.getCity(), room.getId(),
                room.getRoomNumber(), room.getCategory(), room.getCapacity(), room.getPrice());
    }
}
