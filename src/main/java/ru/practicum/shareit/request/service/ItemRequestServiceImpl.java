package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestStorage requestStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    public ItemRequestDto create(Long requestorId, ItemRequestCreateDto dto) {
        User requestor = getUser(requestorId);

        ItemRequest request = ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequest saved = requestStorage.save(request);
        return ItemRequestMapper.toItemRequestDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long requestorId) {
        getUser(requestorId);
        return requestStorage.findByRequestorId(requestorId).stream()
                .map(r -> ItemRequestMapper.toItemRequestDto(r, getItemsForRequest(r.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        getUser(userId);
        return requestStorage.findAllExceptRequestor(userId, from, size).stream()
                .map(r -> ItemRequestMapper.toItemRequestDto(r, getItemsForRequest(r.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUser(userId);
        ItemRequest request = getRequest(requestId);
        return ItemRequestMapper.toItemRequestDto(request, getItemsForRequest(requestId));
    }

    private List<Item> getItemsForRequest(Long requestId) {
        return itemStorage.search("").stream()
                .filter(item -> item.getRequest() != null && item.getRequest().getId().equals(requestId))
                .collect(Collectors.toList());
    }

    private User getUser(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    private ItemRequest getRequest(Long id) {
        return requestStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("ItemRequest не найден: " + id));
    }
}
