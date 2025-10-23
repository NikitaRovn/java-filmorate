package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbc;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbc.query(sql, this::mapRowToDirector);
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        List<Director> directorsList = jdbc.query(sql, this::mapRowToDirector, id);
        return directorsList.stream().findFirst();
    }

    @Override
    public Director createDirector(Director director) {
        String sql = "INSERT INTO directors (director_name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        director.setId(keyHolder.getKey().intValue());

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET director_name = ? WHERE director_id = ?";
        jdbc.update(sql, director.getName(), director.getId());

        return director;
    }

    @Override
    public void deleteDirectorById(int directorId) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        jdbc.update(sql, directorId);
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        Integer id = rs.getInt("director_id");
        String name = rs.getString("director_name");
        return new Director(id, name);
    }
}
