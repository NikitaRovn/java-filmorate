package ru.yandex.practicum.filmorate.model.review;

import lombok.Data;

@Data
public class ReviewReactionId {
    private Long reviewId;
    private Long userId;
}
