package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.ReviewRegisterDto;
import ru.yandex.practicum.filmorate.dto.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.exception.review.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.review.Action;
import ru.yandex.practicum.filmorate.model.review.ReactionType;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.model.review.ReviewReaction;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewReactionStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final ReviewStorage reviewStorage;
    private final ReviewReactionStorage reviewReactionStorage;
    private final EventService eventService;

    public List<Review> getAllReviews(Integer count) {
        return reviewStorage.findAllReviews(count);
    }

    public List<Review> getReviewsByFilmId(Long filmId, Integer count) {
        return reviewStorage.findReviewsByFilmId(filmId, count);
    }

    public Review getReview(Long id) {
        Review review = reviewStorage.findReviewById(id);
        if (review == null) throw new ReviewNotFoundException(id);

        // Формирование пользователя
        Integer userId = review.getUser().getId();
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
        review.setUser(user);

        // Формирование фильма
        Integer filmId = review.getFilm().getId();
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NoSuchElementException("Фильм с ID " + filmId + " не найден"));
        review.setFilm(film);

        // Формирование полезности
        List<ReviewReaction> reviewReactions = reviewReactionStorage.getReviewReactionsByReviewId(review.getId());
        Integer useful = reviewReactions.stream()
                .mapToInt(r -> Boolean.TRUE.equals(r.getIsLike()) ? 1 : -1)
                .sum();
        review.setUseful(useful);

        return review;
    }

    @Transactional
    public Review addReview(ReviewRegisterDto reviewRegisterDto) {
        Review review = ReviewMapper.mapFromReviewRegisterDtoToReview(reviewRegisterDto);
        review.setUseful(0);

        Integer userId = reviewRegisterDto.getUserId().intValue();
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
        review.setUser(user);

        Integer filmId = reviewRegisterDto.getFilmId().intValue();
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NoSuchElementException("Фильм с ID " + filmId + " не найден"));
        review.setFilm(film);

        Review saved = reviewStorage.saveReview(ReviewMapper.mapFromReviewToReviewEntity(review));
        eventService.addReviewEvent(saved.getUser().getId(), saved.getId().intValue(), Event.Operation.ADD);

        return saved;
    }

    public Review updateReview(@Valid ReviewUpdateDto reviewUpdateDto) {
        Review review = reviewStorage.findReviewById(reviewUpdateDto.getReviewId());
        if (review == null) throw new ReviewNotFoundException(reviewUpdateDto.getReviewId());

        if (reviewUpdateDto.getContent() != null) review.setContent(reviewUpdateDto.getContent());
        if (reviewUpdateDto.getIsPositive() != null) review.setIsPositive(reviewUpdateDto.getIsPositive());

        Review updated = reviewStorage.updateReview(ReviewMapper.mapFromReviewToReviewEntity(review));

        eventService.addReviewEvent(updated.getUser().getId(), updated.getId().intValue(), Event.Operation.UPDATE);

        return updated;
    }

    @Transactional
    public void reactionManager(Long reviewId, Long userId, Action action, ReactionType reactionType) {
        ReviewReaction reviewReaction = reviewReactionStorage.getReviewReaction(reviewId, userId);
        Boolean exist = (reviewReaction == null) ? null : reviewReaction.getIsLike();
        int delta = deltaCalculator(action, reactionType, exist);

        if (action == Action.ADD) {
            reviewReactionStorage.saveReviewReaction(reviewId, userId, reactionType.equals(ReactionType.LIKE));
        } else {
            reviewReactionStorage.deleteReviewReaction(reviewId, userId);
        }

        if (delta != 0) reviewStorage.updateReviewUseful(reviewId, delta);
    }

    public void deleteReview(Long id) {
        Review review = reviewStorage.findReviewById(id);
        if (review == null) throw new ReviewNotFoundException(id);
        reviewStorage.deleteReviewById(id);
        eventService.addReviewEvent(review.getUser().getId(), id.intValue(), Event.Operation.REMOVE);
    }

    private static int deltaCalculator(Action action, ReactionType reactionType, Boolean exist) {
        if (action == Action.ADD) {
            if (exist == null) return (reactionType.equals(ReactionType.LIKE)) ? +1 : -1;
            if (Boolean.TRUE.equals(exist)  && reactionType.equals(ReactionType.DISLIKE)) return -2; // like -> dislike
            if (Boolean.FALSE.equals(exist) && reactionType.equals(ReactionType.LIKE))    return +2; // dislike -> like
        } else {
            if (exist == null) return 0;
            if (Boolean.TRUE.equals(exist)  && reactionType.equals(ReactionType.LIKE))    return -1;
            if (Boolean.FALSE.equals(exist) && reactionType.equals(ReactionType.DISLIKE)) return +1;
        }
        return 0;
    }
}
