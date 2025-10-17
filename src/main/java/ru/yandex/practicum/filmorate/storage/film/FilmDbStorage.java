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
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    public static final String FIND_BY_IDS_QUERY = """
            SELECT f.*, r.code as rating_code
            FROM films f
            LEFT JOIN ratings r ON f.rating_id = r.rating_id
            WHERE f.film_id IN (%s)
            """;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "title",
            "director",
            "description"
    );

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, r.code as rating_code" +
                " FROM films f" +
                " LEFT JOIN ratings r ON f.rating_id = r.rating_id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        films.forEach(this::loadFilmGenres);
        films.forEach(this::loadFilmDirectors);
        return films;
    }

    @Override
    public List<Film> findFilmsByIds(Set<Integer> filmIds) {
        if (filmIds.isEmpty()) return List.of();
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = String.format(FIND_BY_IDS_QUERY, placeholders);
        List<Film> films = jdbcTemplate.query(query, this::mapRowToFilm, filmIds.toArray());
        films.forEach(this::loadFilmGenres);
        films.forEach(this::loadFilmDirectors);
        return films;
    }

    @Override
    public List<Film> findByContains(String field, String query) {
        if (!ALLOWED_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Field is not allowed: " + field);
        }
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String like = "%" + query.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_") + "%";

        String sql = switch (field) {
            case "title" -> """
            SELECT f.*, r.code AS rating_code
            FROM films f
            LEFT JOIN ratings r ON r.rating_id = f.rating_id
            WHERE UPPER(f.name) LIKE UPPER(?) ESCAPE '\\'
        """;
            case "description" -> """
            SELECT f.*, r.code AS rating_code
            FROM films f
            LEFT JOIN ratings r ON r.rating_id = f.rating_id
            WHERE UPPER(f.description) LIKE UPPER(?) ESCAPE '\\'
        """;
            case "director" -> """
            SELECT DISTINCT f.*, r.code AS rating_code
            FROM films f
            JOIN film_directors fd ON fd.film_id = f.film_id
            JOIN directors d       ON d.director_id = fd.director_id
            LEFT JOIN ratings r    ON r.rating_id = f.rating_id
            WHERE UPPER(d.director_name) LIKE UPPER(?) ESCAPE '\\'
        """;
            default -> throw new IllegalStateException("Unexpected field: " + field);
        };

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, like);

        films.forEach(this::loadFilmGenres);
        films.forEach(this::loadFilmDirectors);

        return films;
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id)" +
                     " VALUES (?, ?, ?, ?, ?)";

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
        saveFilmDirectors(film);

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?," +
                     " duration = ?, rating_id = ?" +
                     " WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        updateFilmGenres(film);
        updateFilmDirectors(film);

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
        films.forEach(this::loadFilmDirectors);

        return films;
    }

    @Override
    public Optional<Film> findById(Integer id) {
        String sql = "SELECT f.*, r.code as rating_code" +
                " FROM films f" +
                " LEFT JOIN ratings r ON f.rating_id = r.rating_id" +
                " WHERE film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        loadFilmGenres(film);
        loadFilmDirectors(film);

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

    private void saveFilmDirectors(Film film) {
        String deleteSql = "DELETE FROM film_directors WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            String insertSql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
            for (Director director : film.getDirectors()) {
                jdbcTemplate.update(insertSql, film.getId(), director.getId());
            }
        }
    }

    private void updateFilmDirectors(Film film) {
        saveFilmDirectors(film);
    }

    private void loadFilmDirectors(Film film) {
        String sql = "SELECT d.director_id, d.director_name" +
                " FROM film_directors fd" +
                " JOIN directors d ON fd.director_id = d.director_id " +
                " WHERE fd.film_id = ?";

        List<Director> directors = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Integer directorId = rs.getInt("director_id");
            String directorName = rs.getString("director_name");
            return new Director(directorId, directorName);
        }, film.getId());

        film.getDirectors().clear();
        film.getDirectors().addAll(directors);
    }
}
