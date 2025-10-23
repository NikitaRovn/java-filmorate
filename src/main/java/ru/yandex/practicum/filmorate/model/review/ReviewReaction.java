package ru.yandex.practicum.filmorate.model.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReaction {
    private ReviewReactionId id;

    Long reviewId;
    Long userId;
    Boolean isLike;
}
