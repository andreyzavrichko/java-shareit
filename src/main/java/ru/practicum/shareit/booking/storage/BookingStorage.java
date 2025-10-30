package ru.practicum.shareit.booking.storage;

import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingStorage {
    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findByBookerId(Long bookerId, BookingStatus status);

    List<Booking> findByOwnerId(Long ownerId, BookingStatus status);

    List<Booking> findByItemId(Long itemId);

    List<Booking> findApprovedByItemId(Long itemId);
}
