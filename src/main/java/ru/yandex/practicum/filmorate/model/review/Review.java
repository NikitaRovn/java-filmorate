package ru.yandex.practicum.filmorate.model.review;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

@Data
@Builder
public class Review {
    Long id;
    String content;
    Boolean isPositive;
    User user;
    Film film;
    Integer useful;
}
