package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventService eventService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, UserStorage userStorage,
                         FilmStorage filmStorage, EventService eventService) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventService = eventService;
    }

    public Review create(Review review) {
        validateUserExists(review.getUserId());
        validateFilmExists(review.getFilmId());

        Review createdReview = reviewStorage.create(review);
        eventService.addReviewEvent(review.getUserId(), createdReview.getReviewId(), Event.Operation.ADD);
        return createdReview;
    }

    public Review update(Review review) {
        validateReviewExists(review.getReviewId());
        validateUserExists(review.getUserId());
        validateFilmExists(review.getFilmId());

        Review updatedReview = reviewStorage.update(review);
        eventService.addReviewEvent(review.getUserId(), updatedReview.getReviewId(), Event.Operation.UPDATE);
        return updatedReview;
    }

    public void delete(Integer reviewId) {
        Review review = getById(reviewId);
        reviewStorage.delete(reviewId);
        eventService.addReviewEvent(review.getUserId(), reviewId, Event.Operation.REMOVE);
    }

    public Review getById(Integer reviewId) {
        return reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Отзыв с id " + reviewId + " не найден"));
    }

    public List<Review> getReviewsByFilmId(Integer filmId, Integer count) {
        if (filmId != null) {
            validateFilmExists(filmId);
            return reviewStorage.findByFilmId(filmId, count == null ? 10 : count);
        } else {
            return reviewStorage.findAll(count == null ? 10 : count);
        }
    }

    public void addLike(Integer reviewId, Integer userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Integer reviewId, Integer userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Integer reviewId, Integer userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Integer reviewId, Integer userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateReviewExists(Integer reviewId) {
        if (!reviewStorage.existsById(reviewId)) {
            throw new NoSuchElementException("Отзыв с id " + reviewId + " не найден");
        }
    }

    private void validateUserExists(Integer userId) {
        if (!userStorage.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с id " + userId + " не найден");
        }
    }

    private void validateFilmExists(Integer filmId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NoSuchElementException("Фильм с id " + filmId + " не найден");
        }
    }
}