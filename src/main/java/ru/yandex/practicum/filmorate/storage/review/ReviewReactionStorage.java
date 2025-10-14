package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.review.ReviewReaction;

import java.util.List;

public interface ReviewReactionStorage {
    void saveReviewReaction(Long reviewId, Long userId, Boolean like);

    ReviewReaction getReviewReaction(Long reviewId, Long userId);

    List<ReviewReaction> getReviewReactionsByReviewId(Long reviewId);

    void deleteReviewReaction(Long reviewId, Long userId);
}
