package ru.practicum.shareit.booking.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryBookingStorage implements BookingStorage {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private long idCounter = 0;

    @Override
    public Booking save(Booking booking) {
        booking.setId(++idCounter);
        bookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public List<Booking> findByBookerId(Long bookerId, BookingStatus status) {
        return bookings.values().stream()
                .filter(b -> b.getBooker().getId().equals(bookerId))
                .filter(b -> status == null || b.getStatus() == status)
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByOwnerId(Long ownerId, BookingStatus status) {
        return bookings.values().stream()
                .filter(b -> b.getItem().getOwner().getId().equals(ownerId))
                .filter(b -> status == null || b.getStatus() == status)
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByItemId(Long itemId) {
        return bookings.values().stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findApprovedByItemId(Long itemId) {
        return bookings.values().stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .sorted(Comparator.comparing(Booking::getStart))
                .collect(Collectors.toList());
    }
}