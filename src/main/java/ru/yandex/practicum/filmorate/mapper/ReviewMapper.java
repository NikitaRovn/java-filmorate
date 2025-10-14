package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewRegisterDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.model.review.ReviewEntity;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReviewMapper {
    public static ReviewEntity mapFromReviewToReviewEntity(Review review) {
        return ReviewEntity.builder()
                .id(review.getId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(Long.valueOf(review.getUser().getId()))
                .filmId(Long.valueOf(review.getFilm().getId()))
                .useful(review.getUseful())
                .build();
    }

    public static Review mapFromReviewEntityToReview(ReviewEntity reviewEntity) {
        User user = new User();
        user.setId(Math.toIntExact(reviewEntity.getUserId()));
        Film film = new Film();
        film.setId(Math.toIntExact(reviewEntity.getFilmId()));

        return Review.builder()
                .id(reviewEntity.getId())
                .content(reviewEntity.getContent())
                .isPositive(reviewEntity.getIsPositive())
                .user(user)
                .film(film)
                .useful(reviewEntity.getUseful())
                .build();
    }

    public static ReviewDto mapFromReviewToReviewDto(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(Long.valueOf(review.getUser().getId()))
                .filmId(Long.valueOf(review.getFilm().getId()))
                .useful(review.getUseful())
                .build();
    }

    public static List<ReviewDto> mapFromListReviewToListReviewDto(List<Review> reviews) {
        return reviews.stream()
                .map(ReviewMapper::mapFromReviewToReviewDto)
                .toList();
    }

    public static Review mapFromReviewRegisterDtoToReview(ReviewRegisterDto reviewRegisterDto) {
        return Review.builder()
                .content(reviewRegisterDto.getContent())
                .isPositive(reviewRegisterDto.getIsPositive())
                .build();

    }
}
