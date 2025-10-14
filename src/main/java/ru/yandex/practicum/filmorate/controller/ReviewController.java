package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewRegisterDto;
import ru.yandex.practicum.filmorate.dto.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.review.Action;
import ru.yandex.practicum.filmorate.model.review.ReactionType;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public List<ReviewDto> getReviews(@RequestParam(required = false) Long filmId, @RequestParam(defaultValue = "10") Integer count) {
        if (filmId != null) {
            return ReviewMapper.mapFromListReviewToListReviewDto(reviewService.getReviewsByFilmId(filmId, count));
        } else {
            return ReviewMapper.mapFromListReviewToListReviewDto(reviewService.getAllReviews(count));
        }
    }

    @GetMapping("/{id}")
    public ReviewDto getReviewById(@PathVariable Long id) {
        return ReviewMapper.mapFromReviewToReviewDto(reviewService.getReview(id));
    }

    @PostMapping
    public ReviewDto addReview(@Valid @RequestBody ReviewRegisterDto reviewRegisterDto) {
        return ReviewMapper.mapFromReviewToReviewDto(reviewService.addReview(reviewRegisterDto));
    }

    @PutMapping
    public ReviewDto updateReview(@Valid @RequestBody ReviewUpdateDto reviewUpdateDto) {
        return ReviewMapper.mapFromReviewToReviewDto(reviewService.updateReview(reviewUpdateDto));
    }

    @PutMapping("{id}/like/{userId}")
    public void addLikeReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.reactionManager(id, userId, Action.ADD, ReactionType.LIKE);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void addDislikeReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.reactionManager(id, userId, Action.ADD, ReactionType.DISLIKE);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void deleteLikeReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.reactionManager(id, userId, Action.DELETE, ReactionType.LIKE);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void deleteDislikeReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.reactionManager(id, userId, Action.DELETE, ReactionType.DISLIKE);
    }
}
