package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.review.ReviewEntity;

import java.sql.ResultSet;
import java.sql.SQLException;


@Component
public class ReviewEntityRowMapper implements RowMapper<ReviewEntity> {
    @Override
    public ReviewEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ReviewEntity.builder()
                .id(rs.getLong("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
