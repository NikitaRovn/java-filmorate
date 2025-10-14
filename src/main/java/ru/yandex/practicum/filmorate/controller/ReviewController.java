package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        log.info("Получен запрос на создание отзыва: {}", review);
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        log.info("Получен запрос на обновление отзыва: {}", review);
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        log.info("Получен запрос на удаление отзыва с ID: {}", id);
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable Integer id) {
        log.info("Получен запрос на получение отзыва с ID: {}", id);
        return reviewService.getById(id);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(defaultValue = "10") Integer count) {
        log.info("Получен запрос на получение отзывов для фильма ID: {}, количество: {}", filmId, count);
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос на добавление лайка отзыву {} от пользователя {}", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос на добавление дизлайка отзыву {} от пользователя {}", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос на удаление лайка отзыву {} от пользователя {}", id, userId);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос на удаление дизлайка отзыву {} от пользователя {}", id, userId);
        reviewService.removeDislike(id, userId);
    }
}