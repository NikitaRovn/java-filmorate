package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.model.review.ReviewEntity;

import java.util.List;

public interface ReviewStorage {
    Review saveReview(ReviewEntity reviewEntity);

    Review findReviewById(Long id);

    List<Review> findAllReviews(Integer count);

    List<Review> findReviewsByFilmId(Long id, Integer count);

    Review updateReview(ReviewEntity reviewEntity);

    void updateReviewUseful(Long id, int delta);

    void deleteReviewById(Long id);
}
