package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на список всех фильмов. Текущее количество: {}", filmStorage.findAll().size());
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable Integer id) {
        log.info("Получен запрос на получение фильма с ID: {}", id);
        return filmStorage.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Фильм с id " + id + " не найден"));
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateFilmReleaseDate(film);
        Film createdFilm = filmStorage.create(film);
        log.info("Добавлен новый фильм: {}", createdFilm);
        return createdFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (!filmStorage.existsById(film.getId())) {
            log.warn("Попытка обновления несуществующего фильма с ID: {}", film.getId());
            throw new NoSuchElementException("Фильм с id " + film.getId() + " не найден");
        }
        validateFilmReleaseDate(film);
        Film updatedFilm = filmStorage.update(film);
        log.info("Обновлен фильм: {}", updatedFilm);
        return updatedFilm;
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Long id) {
        filmService.deleteFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") Integer count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {

        if (genreId == null && year == null) {
            return filmService.getPopularFilms(count);
        } else {
            return filmService.getPopularFilms(count, genreId, year);
        }
    }

    @GetMapping("/common")
    public List<Film> getMutualFilmOfTwoUser(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getMutualFilmOfTwoUser(userId, friendId);
    }

    private void validateFilmReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Попытка добавить фильм с некорректной датой релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Integer directorId, @RequestParam(value = "sortBy", required = false) String sortBy) {
        if (sortBy != null && sortBy.equals("year")) {
            return filmService.directorFilmsSortedByYear(directorId);
        } else if (sortBy != null && sortBy.equals("likes")) {
            return filmService.directorFilmsSortedByLikes(directorId);
        } else {
            return filmService.getFilmsByDirector(directorId);
        }
    }

    @GetMapping("/search")
    public List<Film> getSearch(@RequestParam String query, @RequestParam(required = false) List<String> by) {
        return filmService.getSearch(query, by);
    }
}
