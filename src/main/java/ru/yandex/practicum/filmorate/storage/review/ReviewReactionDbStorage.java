package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.review.ReviewReaction;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewReactionDbStorage implements ReviewReactionStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<ReviewReaction> mapper;

    public static final String SAVE_REVIEW_REACTION_QUERY = """
            MERGE INTO review_reactions (review_id, user_id, is_like)
            KEY (review_id, user_id)
            VALUES (?, ?, ?)
            """;
    public static final String FIND_REVIEW_REACTIONS_YB_REVIEW_ID_QUERY = """
            SELECT review_id, user_id, is_like
            FROM review_reactions
            WHERE review_id = ?
            """;
    public static final String DELETE_REVIEW_BY_ID_QUERY = """
            DELETE FROM review_reactions
            WHERE review_id = ? AND user_id = ?
            """;
    public static final String FIND_REVIEW_BY_REVIEW_ID_AND_USER_ID_QUERY = """
            SELECT review_id, user_id, is_like
            FROM review_reactions
            WHERE review_id = ? AND user_id = ?
            """;

    @Override
    public void saveReviewReaction(Long reviewId, Long userId, Boolean like) {
        jdbc.update(SAVE_REVIEW_REACTION_QUERY,
                reviewId,
                userId,
                like);
    }

    @Override
    public ReviewReaction getReviewReaction(Long reviewId, Long userId) {
        List<ReviewReaction> reviewReactions = jdbc.query(FIND_REVIEW_BY_REVIEW_ID_AND_USER_ID_QUERY,
                mapper,
                reviewId,
                userId);
        if (reviewReactions.isEmpty()) {
            return null;
        } else {
            return reviewReactions.getFirst();
        }
    }

    @Override
    public List<ReviewReaction> getReviewReactionsByReviewId(Long reviewId) {
        return jdbc.query(FIND_REVIEW_REACTIONS_YB_REVIEW_ID_QUERY, mapper, reviewId);
    }

    @Override
    public void deleteReviewReaction(Long reviewId, Long userId) {
        jdbc.update(DELETE_REVIEW_BY_ID_QUERY, reviewId, userId);
    }
}
