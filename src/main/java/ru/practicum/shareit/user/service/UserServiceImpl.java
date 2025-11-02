package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        if (userStorage.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email уже используется");
        }
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.save(user));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existing = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existing.getEmail())) {
            if (userStorage.existsByEmail(userDto.getEmail())) {
                throw new IllegalArgumentException("Email уже используется");
            }
            existing.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }

        return UserMapper.toUserDto(userStorage.save(existing));
    }

    @Override
    public UserDto getById(Long id) {
        return userStorage.findById(id)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (userStorage.findById(id).isEmpty()) {
            throw new NotFoundException("Пользователь не найден: " + id);
        }
        userStorage.deleteById(id);
    }
}
