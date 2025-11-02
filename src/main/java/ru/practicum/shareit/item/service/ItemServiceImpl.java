package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUser(userId);

        Item item = Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .build();

        return ItemMapper.toItemDto(itemStorage.save(item), null, null);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = getItem(itemId);
        checkOwner(item, userId);

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        return ItemMapper.toItemDto(itemStorage.update(item), null, null);
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        Item item = getItem(itemId);
        Booking lastBooking = null;
        Booking nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> approvedBookings = bookingStorage.findApprovedByItemId(itemId);
            LocalDateTime now = LocalDateTime.now();

            lastBooking = approvedBookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getEnd))
                    .orElse(null);

            nextBooking = approvedBookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .orElse(null);
        }

        return ItemMapper.toItemDto(item, lastBooking, nextBooking);
    }

    @Override
    public List<ItemDto> findByOwnerId(Long ownerId) {
        getUser(ownerId);

        return itemStorage.findByOwnerId(ownerId).stream()
                .map(item -> {
                    List<Booking> approvedBookings = bookingStorage.findApprovedByItemId(item.getId());
                    LocalDateTime now = LocalDateTime.now();

                    Booking last = approvedBookings.stream()
                            .filter(b -> b.getEnd().isBefore(now))
                            .max(Comparator.comparing(Booking::getEnd))
                            .orElse(null);

                    Booking next = approvedBookings.stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .min(Comparator.comparing(Booking::getStart))
                            .orElse(null);

                    return ItemMapper.toItemDto(item, last, next);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemStorage.search(text).stream()
                .map(item -> ItemMapper.toItemDto(item, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId, Long itemId) {
        Item item = getItem(itemId);
        checkOwner(item, userId);
        itemStorage.deleteById(itemId);
    }


    private User getUser(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    private Item getItem(Long id) {
        return itemStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + id));
    }

    private void checkOwner(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только владелец может редактировать вещь");
        }
    }
}