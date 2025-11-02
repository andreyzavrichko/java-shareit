package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findByOwnerId(Long ownerId);  // ← оставляем так

    List<Item> search(String text);

    Item update(Item item);

    void deleteById(Long id);
}
