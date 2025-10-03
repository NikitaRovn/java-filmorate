package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.AgeRating;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(new AgeRating(3, "PG-13", "PG-13"));
        testFilm.addGenre(new Genre(1, "Комедия"));
        testFilm.addGenre(new Genre(2, "Драма"));
    }

    @Test
    void testCreateFilm() {
        Film createdFilm = filmStorage.create(testFilm);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo(testFilm.getName());
        assertThat(createdFilm.getDescription()).isEqualTo(testFilm.getDescription());
        assertThat(createdFilm.getReleaseDate()).isEqualTo(testFilm.getReleaseDate());
        assertThat(createdFilm.getDuration()).isEqualTo(testFilm.getDuration());
        assertThat(createdFilm.getMpa().getId()).isEqualTo(3); // Проверяем ID рейтинга
        assertThat(createdFilm.getGenres()).hasSize(2);
    }

    @Test
    void testUpdateFilm() {
        Film createdFilm = filmStorage.create(testFilm);

        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated Description");
        createdFilm.getGenres().clear();
        createdFilm.addGenre(new Genre(6, "Боевик")); // Используем новый конструктор

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedFilm.getGenres()).hasSize(1);
        assertThat(updatedFilm.getGenres().iterator().next().getId()).isEqualTo(6);
    }

    @Test
    void testFindFilmById() {
        Film createdFilm = filmStorage.create(testFilm);
        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getId()).isEqualTo(createdFilm.getId());
                    assertThat(film.getName()).isEqualTo(createdFilm.getName());
                    assertThat(film.getGenres()).hasSize(2);
                    assertThat(film.getMpa().getId()).isEqualTo(3);
                });
    }

    @Test
    void testFindAllFilms() {
        Film film1 = filmStorage.create(testFilm);

        Film film2 = new Film();
        film2.setName("Another Film");
        film2.setDescription("Another Description");
        film2.setReleaseDate(LocalDate.of(2010, 1, 1));
        film2.setDuration(90);
        film2.setMpa(new AgeRating(1, "G", "G")); // Используем новый конструктор
        filmStorage.create(film2);

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getId).contains(film1.getId());
    }

    @Test
    void testExistsById() {
        Film createdFilm = filmStorage.create(testFilm);

        boolean exists = filmStorage.existsById(createdFilm.getId());
        boolean notExists = filmStorage.existsById(999);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindFilmById_NotFound() {
        Optional<Film> foundFilm = filmStorage.findById(999);

        assertThat(foundFilm).isEmpty();
    }
}