package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.AgeRating;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import java.time.LocalDate;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({LikeDbStorage.class, FilmDbStorage.class, UserDbStorage.class})
class LikeDbStorageTest {

    private final LikeDbStorage likeStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film testFilm;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testFilm = createFilm("Test Film", "Test Description", new AgeRating(3, "PG-13", "PG-13"));
        testUser1 = createUser("user1@example.com", "user1", "User One");
        testUser2 = createUser("user2@example.com", "user2", "User Two");
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

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    @Test
    void testAddLike() {
        likeStorage.addLike(testFilm.getId(), testUser1.getId());

        boolean hasLike = likeStorage.hasLike(testFilm.getId(), testUser1.getId());
        int likesCount = likeStorage.getLikesCount(testFilm.getId());

        assertThat(hasLike).isTrue();
        assertThat(likesCount).isEqualTo(1);
    }

    @Test
    void testRemoveLike() {
        likeStorage.addLike(testFilm.getId(), testUser1.getId());
        likeStorage.removeLike(testFilm.getId(), testUser1.getId());

        boolean hasLike = likeStorage.hasLike(testFilm.getId(), testUser1.getId());
        int likesCount = likeStorage.getLikesCount(testFilm.getId());

        assertThat(hasLike).isFalse();
        assertThat(likesCount).isEqualTo(0);
    }

    @Test
    void testHasLike_False() {
        boolean hasLike = likeStorage.hasLike(testFilm.getId(), testUser1.getId());

        assertThat(hasLike).isFalse();
    }

    @Test
    void testGetLikesCount_MultipleLikes() {
        likeStorage.addLike(testFilm.getId(), testUser1.getId());
        likeStorage.addLike(testFilm.getId(), testUser2.getId());

        int likesCount = likeStorage.getLikesCount(testFilm.getId());

        assertThat(likesCount).isEqualTo(2);
    }

    @Test
    void testGetLikedUserIds() {
        likeStorage.addLike(testFilm.getId(), testUser1.getId());
        likeStorage.addLike(testFilm.getId(), testUser2.getId());

        Set<Integer> likedUserIds = likeStorage.getLikedUserIds(testFilm.getId());

        assertThat(likedUserIds).hasSize(2);
        assertThat(likedUserIds).contains(testUser1.getId(), testUser2.getId());
    }

    @Test
    void testGetLikesCount_NoLikes() {
        int likesCount = likeStorage.getLikesCount(testFilm.getId());

        assertThat(likesCount).isEqualTo(0);
    }

    @Test
    void testGetLikedUserIds_NoLikes() {
        Set<Integer> likedUserIds = likeStorage.getLikedUserIds(testFilm.getId());

        assertThat(likedUserIds).isEmpty();
    }
}