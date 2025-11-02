package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    public BookingDto create(Long bookerId, BookingShortDto dto) {
        User booker = getUser(bookerId);
        Item item = getItem(dto.getItemId());

        if (!item.getAvailable()) {
            throw new IllegalStateException("Вещь недоступна для букинга");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new IllegalStateException("Владелец не может забронировать свой собственный товар");
        }


        if (bookingStorage.findByItemId(item.getId()).stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.APPROVED &&
                        isOverlap(dto.getStart(), dto.getEnd(), b.getStart(), b.getEnd()))) {
            throw new IllegalStateException("Вещь была зарезервирована на эти даты");
        }

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        return BookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = getBooking(bookingId);
        Item item = booking.getItem();

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("Только владелец может принять букинг");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalStateException("Букинг уже в процессе");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = getBooking(bookingId);
        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();

        if (!ownerId.equals(userId) && !bookerId.equals(userId)) {
            throw new IllegalStateException("Пользователь не имеет доступ");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long bookerId, String state) {
        getUser(bookerId);
        BookingStatus status = parseState(state);
        return bookingStorage.findByBookerId(bookerId, status).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getByOwner(Long ownerId, String state) {
        getUser(ownerId);
        BookingStatus status = parseState(state);
        return bookingStorage.findByOwnerId(ownerId, status).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private BookingStatus parseState(String state) {
        if (state == null || state.isBlank() || "ALL".equalsIgnoreCase(state)) {
            return BookingStatus.ALL;
        }
        try {
            return BookingStatus.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный статус " + state);
        }
    }

    private boolean isOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    private User getUser(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));
    }

    private Item getItem(Long id) {
        return itemStorage.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Вещь не найдена: " + id));
    }

    private Booking getBooking(Long id) {
        return bookingStorage.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Букинг не найден: " + id));
    }
}
