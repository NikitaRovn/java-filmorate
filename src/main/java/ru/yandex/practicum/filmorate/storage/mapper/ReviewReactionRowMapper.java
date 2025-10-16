package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.review.ReviewReaction;
import ru.yandex.practicum.filmorate.model.review.ReviewReactionId;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewReactionRowMapper implements RowMapper<ReviewReaction> {
    @Override
    public ReviewReaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReviewReactionId id = new ReviewReactionId();
        id.setReviewId(rs.getLong("review_id"));
        id.setUserId(rs.getLong("user_id"));

        return ReviewReaction.builder()
                .id(id)
                .reviewId(rs.getLong("review_id"))
                .userId(rs.getLong("user_id"))
                .isLike(rs.getBoolean("is_like"))
                .build();
    }
}
