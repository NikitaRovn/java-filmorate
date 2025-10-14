package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(Integer reviewId);

    Optional<Review> findById(Integer reviewId);

    List<Review> findByFilmId(Integer filmId, Integer count);

    List<Review> findAll(Integer count);

    void addLike(Integer reviewId, Integer userId);

    void addDislike(Integer reviewId, Integer userId);

    void removeLike(Integer reviewId, Integer userId);

    void removeDislike(Integer reviewId, Integer userId);

    boolean existsById(Integer reviewId);
}