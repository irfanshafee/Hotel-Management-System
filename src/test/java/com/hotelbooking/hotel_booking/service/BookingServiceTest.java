package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.CreateBookingRequest;
import com.hotelbooking.hotel_booking.entity.Booking;
import com.hotelbooking.hotel_booking.entity.Hotel;
import com.hotelbooking.hotel_booking.entity.Room;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.RoomCategory;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.RoomRepository;
import com.hotelbooking.hotel_booking.repository.UserRepository;
import com.hotelbooking.hotel_booking.security.AuthenticatedUser;
import com.hotelbooking.hotel_booking.security.AuthenticatedUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceTest {
    private BookingRepository bookingRepository;
    private RoomRepository roomRepository;
    private UserRepository userRepository;
    private AuthenticatedUserContext authenticatedUserContext;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        roomRepository = mock(RoomRepository.class);
        userRepository = mock(UserRepository.class);
        authenticatedUserContext = mock(AuthenticatedUserContext.class);
        bookingService = new BookingService(
                bookingRepository, roomRepository, userRepository, authenticatedUserContext);
        when(authenticatedUserContext.getRequiredUser())
                .thenReturn(new AuthenticatedUser(1L, "Test User", "test@example.com", null));
    }

    @Test
    void bookingCreationLoadsRoomWithOptimisticForceIncrement() {
        User user = user();
        Room room = room();
        CreateBookingRequest request = request();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findByIdForBooking(3L)).thenReturn(Optional.of(room));
        when(bookingRepository.countOverlappingActiveBookings(
                any(), any(), any(), anyList())).thenReturn(0L);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(25L);
            return booking;
        });

        var response = bookingService.createBooking(request);

        assertEquals(BookingStatus.PENDING, response.status());
        verify(roomRepository).findByIdForBooking(3L);
        verify(roomRepository, never()).findById(3L);
    }

    @Test
    void overlappingBookingIsRejectedBeforeInsert() {
        CreateBookingRequest request = request();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(roomRepository.findByIdForBooking(3L)).thenReturn(Optional.of(room()));
        when(bookingRepository.countOverlappingActiveBookings(
                any(), any(), any(), anyList())).thenReturn(1L);

        ApiException exception = assertThrows(
                ApiException.class, () -> bookingService.createBooking(request));

        assertEquals("room.unavailable", exception.getMessageKey());
        verify(bookingRepository, never()).save(any());
    }

    private CreateBookingRequest request() {
        return new CreateBookingRequest(
                3L, LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        return user;
    }

    private Room room() {
        Hotel hotel = new Hotel();
        hotel.setId(2L);
        hotel.setName("Test Hotel");
        hotel.setCity("Dhaka");

        Room room = new Room();
        room.setId(3L);
        room.setHotel(hotel);
        room.setRoomNumber("101");
        room.setCategory(RoomCategory.NORMAL);
        room.setCapacity(2);
        room.setPrice(new BigDecimal("3000.00"));
        return room;
    }
}
