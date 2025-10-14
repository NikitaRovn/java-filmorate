package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            stmt.setInt(5, review.getUseful());
            return stmt;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().intValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        return findById(review.getReviewId()).orElse(review);
    }

    @Override
    public void delete(Integer reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, reviewId);
    }

    @Override
    public Optional<Review> findById(Integer reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, this::mapRowToReview, reviewId);
        return reviews.stream().findFirst();
    }

    @Override
    public List<Review> findByFilmId(Integer filmId, Integer count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public List<Review> findAll(Integer count) {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, count);
    }

    @Override
    public void addLike(Integer reviewId, Integer userId) {
        String deleteSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(deleteSql, reviewId, userId);

        String insertSql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)";
        jdbcTemplate.update(insertSql, reviewId, userId);
        updateUseful(reviewId);
    }

    @Override
    public void addDislike(Integer reviewId, Integer userId) {
        String deleteSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(deleteSql, reviewId, userId);

        String insertSql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)";
        jdbcTemplate.update(insertSql, reviewId, userId);
        updateUseful(reviewId);
    }

    @Override
    public void removeLike(Integer reviewId, Integer userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUseful(reviewId);
    }

    @Override
    public void removeDislike(Integer reviewId, Integer userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUseful(reviewId);
    }

    @Override
    public boolean existsById(Integer reviewId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        return count != null && count > 0;
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getInt("user_id"));
        review.setFilmId(rs.getInt("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }

    private void updateUseful(Integer reviewId) {
        String countLikesSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = true";
        String countDislikesSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = false";

        Integer likes = jdbcTemplate.queryForObject(countLikesSql, Integer.class, reviewId);
        Integer dislikes = jdbcTemplate.queryForObject(countDislikesSql, Integer.class, reviewId);

        int useful = (likes != null ? likes : 0) - (dislikes != null ? dislikes : 0);

        String updateSql = "UPDATE reviews SET useful = ? WHERE review_id = ?";
        jdbcTemplate.update(updateSql, useful, reviewId);
    }
}