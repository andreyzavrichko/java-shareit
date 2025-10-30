package ru.practicum.shareit.request.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryItemRequestStorage implements ItemRequestStorage {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private long idCounter = 0;

    @Override
    public ItemRequest save(ItemRequest request) {
        request.setId(++idCounter);
        requests.put(request.getId(), request);
        return request;
    }

    @Override
    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(requests.get(id));
    }

    @Override
    public List<ItemRequest> findByRequestorId(Long requestorId) {
        return requests.values().stream()
                .filter(r -> r.getRequestor().getId().equals(requestorId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequest> findAllExceptRequestor(Long requestorId, int from, int size) {
        return requests.values().stream()
                .filter(r -> !r.getRequestor().getId().equals(requestorId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }
}
