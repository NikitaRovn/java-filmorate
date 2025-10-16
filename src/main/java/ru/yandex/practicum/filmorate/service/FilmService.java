package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;
    private final DirectorStorage directorStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, LikeStorage likeStorage, DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeStorage;
        this.directorStorage = directorStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);

        if (likeStorage.hasLike(filmId, userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            return;
        }

        likeStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);

        likeStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        likeStorage.getLikesCount(f2.getId()),
                        likeStorage.getLikesCount(f1.getId())
                ))
                .limit(count)
                .collect(Collectors.toList());
    }

    public int getLikesCount(Integer filmId) {
        validateFilmExists(filmId);
        return likeStorage.getLikesCount(filmId);
    }

    private void validateFilmExists(Integer filmId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NoSuchElementException("Фильм с id " + filmId + " не найден");
        }
    }

    private void validateUserExists(Integer userId) {
        if (!userStorage.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с id " + userId + " не найден");
        }
    }

    public List<Film> DirectorFilmsSortedByYear(Integer directorId) {
        return getFilmsByDirector(directorId).stream()
                .sorted(Comparator.comparingInt(f -> f.getReleaseDate().getYear()))
                .toList();
    }

    public List<Film> DirectorFilmsSortedByLikes(Integer directorId) {
        return getFilmsByDirector(directorId).stream()
                .sorted((f1, f2) -> Integer.compare(
                        likeStorage.getLikesCount(f2.getId()),
                        likeStorage.getLikesCount(f1.getId())
                ))
                .toList();
    }

    public List<Film> getFilmsByDirector(Integer directorId) {
        Director director = directorStorage.getDirectorById(directorId)
                .orElseThrow(() -> new NoSuchElementException("Указанный режиссёр не найден"));
        return filmStorage.findAll()
                .stream()
                .filter(film -> film.getDirectors().contains(director))
                .collect(Collectors.toList());
    }
}