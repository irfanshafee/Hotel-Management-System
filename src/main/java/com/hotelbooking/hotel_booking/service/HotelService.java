package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.HotelResponse;
import com.hotelbooking.hotel_booking.entity.Hotel;
import com.hotelbooking.hotel_booking.exception.HotelNotFoundException;
import com.hotelbooking.hotel_booking.exception.InvalidFilterException;
import com.hotelbooking.hotel_booking.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HotelService {
    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public List<HotelResponse> getAllHotels() {
        return hotelRepository.findAll().stream().map(this::toResponse).toList();
    }

    public HotelResponse getHotelById(Long id) {
        return hotelRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new HotelNotFoundException(id));
    }

    public List<HotelResponse> searchHotels(String city, String name) {
        String normalizedCity = normalize(city);
        String normalizedName = normalize(name);
        if (normalizedCity == null && normalizedName == null) {
            throw new InvalidFilterException("At least one search parameter, city or name, is required");
        }

        List<Hotel> hotels;
        if (normalizedCity != null && normalizedName != null) {
            hotels = hotelRepository.findByCityIgnoreCaseAndNameContainingIgnoreCase(
                    normalizedCity, normalizedName);
        } else if (normalizedCity != null) {
            hotels = hotelRepository.findByCityIgnoreCase(normalizedCity);
        } else {
            hotels = hotelRepository.findByNameContainingIgnoreCase(normalizedName);
        }
        return hotels.stream().map(this::toResponse).toList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private HotelResponse toResponse(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(), hotel.getName(), hotel.getCity(),
                hotel.getAddress(), hotel.getDescription());
    }
}
