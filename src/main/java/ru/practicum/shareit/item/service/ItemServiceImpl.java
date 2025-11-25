package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.CommentNotAllowedException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.toCommentDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    @Transactional
    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUser(userId);

        Item item = Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .build();

        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = getItem(itemId);
        checkOwner(item, userId);

        Optional.ofNullable(itemDto.getName()).ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(item::setAvailable);

        Item updated = itemRepository.save(item);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        getUser(userId);
        Item item = getItem(itemId);
        return ItemMapper.toItemDto(item);
    }


    @Override
    public ItemWithBookingsDto findByIdWithDetails(Long userId, Long itemId) {
        Item item = getItem(itemId);
        getUser(userId);

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            lastBooking = getLastBooking(itemId);
            nextBooking = getNextBooking(itemId);
        }

        List<CommentDto> comments = getComments(itemId);

        return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
    }


    @Override
    public List<ItemWithBookingsDto> findByOwnerId(Long ownerId) {
        getUser(ownerId);

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(item -> {
                    BookingShortDto last = getLastBooking(item.getId());
                    BookingShortDto next = getNextBooking(item.getId());
                    List<CommentDto> comments = getComments(item.getId());
                    return ItemMapper.toItemWithBookingsDto(item, last, next, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void delete(Long userId, Long itemId) {
        Item item = getItem(itemId);
        checkOwner(item, userId);
        itemRepository.deleteById(itemId);
    }

    @Transactional
    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {
        User author = getUser(userId);
        Item item = getItem(itemId);

        boolean hasCompletedBooking = bookingRepository.findByItemId(itemId).stream()
                .anyMatch(b ->
                        b.getBooker().getId().equals(userId) &&
                                b.getStatus() == BookingStatus.APPROVED &&
                                b.getEnd().isBefore(LocalDateTime.now())
                );

        if (!hasCompletedBooking) {
            throw new CommentNotAllowedException(
                    "Отзыв можно оставить только после завершения бронирования"
            );
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);
        return toCommentDto(saved);
    }

    private BookingShortDto getLastBooking(Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository
                .findByItemIdAndStatusAndEndBefore(
                        itemId,
                        BookingStatus.APPROVED,
                        now,
                        Sort.by(Sort.Direction.DESC, "end")
                )
                .stream()
                .findFirst()
                .map(ItemMapper::toBookingShortDto)
                .orElse(null);
    }

    private BookingShortDto getNextBooking(Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository
                .findByItemIdAndStatusAndStartAfter(
                        itemId,
                        BookingStatus.APPROVED,
                        now,
                        Sort.by(Sort.Direction.ASC, "start")
                )
                .stream()
                .findFirst()
                .map(ItemMapper::toBookingShortDto)
                .orElse(null);
    }

    private List<CommentDto> getComments(Long itemId) {
        return commentRepository.findByItemId(itemId).stream()
                .map(ItemMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    private Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + id));
    }

    private void checkOwner(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только владелец может редактировать вещь");
        }
    }


}