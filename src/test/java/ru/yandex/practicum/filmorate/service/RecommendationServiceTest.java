package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.AgeRating;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({RecommendationService.class, FilmDbStorage.class, UserDbStorage.class, LikeDbStorage.class})
class RecommendationServiceTest {
    private final RecommendationService recommendationService;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final LikeDbStorage likeStorage;
    private final JdbcTemplate jdbcTemplate;

    private User user1;
    private User user2;
    private User user3;
    private Film film1;
    private Film film2;
    private Film film3;
    private Film film4;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");

        user1 = createUser("user1@example.com", "user1", "User One");
        user2 = createUser("user2@example.com", "user2", "User Two");
        user3 = createUser("user3@example.com", "user3", "User Three");

        film1 = createFilm("Film 1", "Description 1", new AgeRating(1, "G", "G"));
        film2 = createFilm("Film 2", "Description 2", new AgeRating(2, "PG", "PG"));
        film3 = createFilm("Film 3", "Description 3", new AgeRating(3, "PG-13", "PG-13"));
        film4 = createFilm("Film 4", "Description 4", new AgeRating(4, "R", "R"));
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    private Film createFilm(String name, String description, AgeRating rating) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(rating);
        return filmStorage.create(film);
    }

    @Test
    void testGetRecommendations_WithSimilarUser() {
        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film2.getId(), user1.getId());

        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user2.getId());
        likeStorage.addLike(film3.getId(), user2.getId());

        likeStorage.addLike(film1.getId(), user3.getId());
        likeStorage.addLike(film4.getId(), user3.getId());

        List<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.get(0).getId()).isEqualTo(film3.getId());
    }

    @Test
    void testGetRecommendations_NoSimilarUsers() {
        likeStorage.addLike(film1.getId(), user1.getId());

        likeStorage.addLike(film4.getId(), user2.getId());

        List<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    void testGetRecommendations_UserWithoutLikes() {
        List<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    void testGetRecommendations_AllUsersHaveSameLikes() {
        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film2.getId(), user1.getId());

        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user2.getId());

        likeStorage.addLike(film1.getId(), user3.getId());
        likeStorage.addLike(film2.getId(), user3.getId());

        List<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).isEmpty();
    }
}