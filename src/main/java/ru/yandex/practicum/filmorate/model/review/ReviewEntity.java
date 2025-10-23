package ru.yandex.practicum.filmorate.model.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewEntity {
    Long id;
    String content;
    Boolean isPositive;
    Long userId;
    Long filmId;
    Integer useful;
}
