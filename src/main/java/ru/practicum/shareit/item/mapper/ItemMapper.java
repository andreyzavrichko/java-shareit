package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.BookingShort;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public final class ItemMapper {
    private ItemMapper() {
    }

    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking) {
        BookingShort last = lastBooking != null
                ? BookingShort.builder()
                .id(lastBooking.getId())
                .bookerId(lastBooking.getBooker().getId())
                .build()
                : null;

        BookingShort next = nextBooking != null
                ? BookingShort.builder()
                .id(nextBooking.getId())
                .bookerId(nextBooking.getBooker().getId())
                .build()
                : null;

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .lastBooking(last)
                .nextBooking(next)
                .build();
    }
}