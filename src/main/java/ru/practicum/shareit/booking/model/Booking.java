package ru.practicum.shareit.booking.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private Long id;

    @NotNull(message = "Дата начала обязательна")
    @FutureOrPresent(message = "Дата начала не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания обязательна")
    @FutureOrPresent(message = "Дата окончания не может быть в прошлом")
    private LocalDateTime end;

    @NotNull(message = "Вещь обязательна")
    private Item item;

    @NotNull(message = "Букер обязателен")
    private User booker;

    @NotNull(message = "Статус обязателен")
    private BookingStatus status;
}
