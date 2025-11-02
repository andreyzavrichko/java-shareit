package ru.practicum.shareit.request.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    private Long id;

    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;

    @NotNull(message = "Создатель запроса обязателен")
    private User requestor;

    @NotNull(message = "Дата создания обязательна")
    @PastOrPresent(message = "Дата создания не может быть в будущем")
    private LocalDateTime created;
}
