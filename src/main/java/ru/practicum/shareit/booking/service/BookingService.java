package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long bookerId, BookingShortDto dto);

    BookingDto approve(Long ownerId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getByBooker(Long bookerId, String state);

    List<BookingDto> getByOwner(Long ownerId, String state);
}
