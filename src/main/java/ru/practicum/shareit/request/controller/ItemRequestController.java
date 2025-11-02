package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.config.HeaderConstants;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long requestorId,
            @Valid @RequestBody ItemRequestCreateDto dto) {
        return requestService.create(requestorId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long requestorId) {
        return requestService.getOwnRequests(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return requestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
            @PathVariable Long requestId) {
        return requestService.getById(userId, requestId);
    }
}