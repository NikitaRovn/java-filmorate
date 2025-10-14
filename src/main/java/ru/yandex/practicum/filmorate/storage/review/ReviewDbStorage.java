package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.model.review.ReviewEntity;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<ReviewEntity> mapper;

    public static final String FIND_REVIEW_BY_ID_QUERY = """
            SELECT id, content, is_positive, user_id, film_id, useful
            FROM reviews
            WHERE id = ?
            """;
    public static final String FIND_ALL_REVIEWS_LIMITED_QUERY = """
            SELECT id, content, is_positive, user_id, film_id, useful
            FROM reviews
            ORDER BY useful DESC
            LIMIT ?
            """;
    public static final String FIND_REVIEWS_BY_FILM_ID_QUERY = """
            SELECT id, content, is_positive, user_id, film_id, useful
            FROM reviews
            WHERE film_id = ?
            ORDER BY useful DESC
            LIMIT ?
            """;
    public static final String UPDATE_REVIEW_BY_ID_QUERY = """
            UPDATE reviews
            SET content = ?, is_positive = ?
            WHERE id = ?
            """;
    public static final String SAVE_REVIEW_QUERY = """
            INSERT INTO reviews(content, is_positive, user_id, film_id, useful)
            VALUES (?, ?, ?, ?, ?)
            """;
    public static final String DELETE_REVIEW_BY_ID_QUERY = """
            DELETE FROM reviews
            WHERE id = ?
            """;

    @Override
    public Review saveReview(ReviewEntity reviewEntity) {
        Long reviewId = insert(SAVE_REVIEW_QUERY,
                reviewEntity.getContent(),
                reviewEntity.getIsPositive(),
                reviewEntity.getUserId(),
                reviewEntity.getFilmId(),
                0);

        return findReviewById(reviewId);
    }

    @Override
    public Review findReviewById(Long id) {
        List<ReviewEntity> response = jdbc.query(FIND_REVIEW_BY_ID_QUERY, mapper, id);
        if (response.isEmpty()) return null;

        return ReviewMapper.mapFromReviewEntityToReview(response.getFirst());
    }

    @Override
    public List<Review> findAllReviews(Integer count) {
        List<ReviewEntity> reviewEntities = jdbc.query(FIND_ALL_REVIEWS_LIMITED_QUERY, mapper, count);

        return reviewEntities.stream().map(ReviewMapper::mapFromReviewEntityToReview).toList();
    }

    @Override
    public List<Review> findReviewsByFilmId(Long id, Integer count) {
        List<ReviewEntity> reviewEntities = jdbc.query(FIND_REVIEWS_BY_FILM_ID_QUERY, mapper, id, count);

        return reviewEntities.stream().map(ReviewMapper::mapFromReviewEntityToReview).toList();
    }

    @Override
    public Review updateReview(ReviewEntity reviewEntity) {
        jdbc.update(UPDATE_REVIEW_BY_ID_QUERY,
                reviewEntity.getContent(),
                reviewEntity.getIsPositive(),
                reviewEntity.getId());

        return findReviewById(reviewEntity.getId());
    }

    @Override
    public void updateReviewUseful(Long id, int delta) {
        String sql = "UPDATE reviews SET useful = useful + ? WHERE id = ?";
        jdbc.update(sql, delta, id);
    }

    @Override
    public void deleteReviewById(Long id) {
        jdbc.update(DELETE_REVIEW_BY_ID_QUERY, id);
    }

    private Long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        return keyHolder.getKeyAs(Long.class);
    }
}
