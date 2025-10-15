package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.AgeRating;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public static final String FIND_BY_IDS_QUERY = """
            SELECT f.*, r.code as rating_code
            FROM films f
            LEFT JOIN ratings r ON f.rating_id = r.rating_id
            WHERE f.film_id IN (%s)
            """;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, r.code as rating_code FROM films f LEFT JOIN ratings r ON f.rating_id = r.rating_id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        films.forEach(this::loadFilmGenres);
        return films;
    }

    @Override
    public List<Film> findFilmsByIds(Set<Integer> filmIds) {
        if (filmIds.isEmpty()) return List.of();
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = String.format(FIND_BY_IDS_QUERY, placeholders);
        List<Film> films = jdbcTemplate.query(query, this::mapRowToFilm, filmIds.toArray());
        films.forEach(this::loadFilmGenres);
        return films;
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());

        saveFilmGenres(film);

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        updateFilmGenres(film);

        return film;
    }

    @Override
    public List<Film> findPopularFilms(int count, Integer genreId, Integer year) {
        String sql = """
                    SELECT f.*, r.code as rating_code, COUNT(l.user_id) as likes_count
                    FROM films f
                    LEFT JOIN ratings r ON f.rating_id = r.rating_id
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    WHERE 1=1
                """;

        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        if (genreId != null) {
            conditions.add("EXISTS (SELECT 1 FROM film_genres fg WHERE fg.film_id = f.film_id AND fg.genre_id = :genreId)");
            params.put("genreId", genreId);
        }

        if (year != null) {
            conditions.add("YEAR(f.release_date) = :year");
            params.put("year", year);
        }

        if (!conditions.isEmpty()) {
            sql += " AND " + String.join(" AND ", conditions);
        }

        sql += """
                    GROUP BY f.film_id, r.code
                    ORDER BY likes_count DESC, f.film_id
                    LIMIT :count
                """;
        params.put("count", count);

        MapSqlParameterSource paramSource = new MapSqlParameterSource(params);

        List<Film> films = namedParameterJdbcTemplate.query(sql, paramSource, this::mapRowToFilm);
        films.forEach(this::loadFilmGenres);

        return films;
    }

    @Override
    public Optional<Film> findById(Integer id) {
        String sql = "SELECT f.*, r.code as rating_code FROM films f LEFT JOIN ratings r ON f.rating_id = r.rating_id WHERE film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        loadFilmGenres(film);

        return Optional.of(film);
    }

    @Override
    public void deleteFilmById(Long id) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ?", id);
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", id);
        jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", id);
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Integer ratingId = rs.getInt("rating_id");
        String ratingCode = rs.getString("rating_code");
        if (ratingCode != null) {
            film.setMpa(AgeRating.fromId(ratingId));
        }

        return film;
    }

    private void saveFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(insertSql, film.getId(), genre.getId());
            }
        }
    }

    private void updateFilmGenres(Film film) {
        saveFilmGenres(film);
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Integer genreId = rs.getInt("genre_id");
            String genreName = rs.getString("name");
            return new Genre(genreId, genreName);
        }, film.getId());

        film.getGenres().clear();
        film.getGenres().addAll(genres);
    }
}