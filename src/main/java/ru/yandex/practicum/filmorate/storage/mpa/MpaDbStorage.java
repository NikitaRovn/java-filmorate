package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.AgeRating;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AgeRating> getAllMpa() {
        String sql = "SELECT * FROM ratings ORDER BY rating_id";
        return jdbcTemplate.query(sql, this::mapRowToAgeRating);
    }

    @Override
    public Optional<AgeRating> getMpaById(Integer id) {
        String sql = "SELECT * FROM ratings WHERE rating_id = ?";
        List<AgeRating> ratings = jdbcTemplate.query(sql, this::mapRowToAgeRating, id);
        return ratings.stream().findFirst();
    }

    private AgeRating mapRowToAgeRating(ResultSet rs, int rowNum) throws SQLException {
        Integer id = rs.getInt("rating_id");
        String code = rs.getString("code");
        return new AgeRating(id, code, code);
    }
}