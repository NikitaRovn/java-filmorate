package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewRegisterDto {
    @NotNull(message = "Поле content должно быть передано.")
    String content;

    @NotNull(message = "Поле content должно быть передано.")
    Boolean isPositive;

    @NotNull(message = "Поле content должно быть передано.")
    Long userId;

    @NotNull(message = "Поле content должно быть передано.")
    Long filmId;
}
