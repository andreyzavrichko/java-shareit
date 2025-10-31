package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.config.HeaderConstants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
            @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
            @PathVariable Long itemId) {
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getAllByOwner(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId) {
        return itemService.findByOwnerId(userId);  // ← изменил имя
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
            @PathVariable Long itemId) {
        itemService.delete(userId, itemId);
    }
}