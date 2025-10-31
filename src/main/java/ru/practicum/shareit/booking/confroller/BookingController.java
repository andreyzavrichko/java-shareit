package ru.practicum.shareit.booking.confroller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.config.HeaderConstants;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long bookerId,
            @Valid @RequestBody BookingShortDto dto) {
        return bookingService.create(bookerId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
            @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getByBooker(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long bookerId,
            @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getByBooker(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwner(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long ownerId,
            @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getByOwner(ownerId, state);
    }
}
