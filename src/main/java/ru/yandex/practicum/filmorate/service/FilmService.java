package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;
    private final EventService eventService;
    private final DirectorStorage directorStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       LikeStorage likeStorage,
                       EventService eventService,
                       DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeStorage;
        this.eventService = eventService;
        this.directorStorage = directorStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);

        boolean alreadyLiked = likeStorage.hasLike(filmId, userId);

        if (!alreadyLiked) {
            likeStorage.addLike(filmId, userId);
            eventService.addLikeEvent(userId, filmId, Event.Operation.ADD);
        } else {
            eventService.addLikeEvent(userId, filmId, Event.Operation.ADD);
        }
    }

    public void removeLike(Integer filmId, Integer userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);

        likeStorage.removeLike(filmId, userId);
        eventService.addLikeEvent(userId, filmId, Event.Operation.REMOVE);
        log.info("Создано событие LIKE: пользователь {} удалил лайк фильму {} (operation: REMOVE, entityId: {})",
                userId,
                filmId,
                filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) ->
                        Integer.compare(likeStorage.getLikesCount(f2.getId()), likeStorage.getLikesCount(f1.getId())))
                .limit(count)
                .toList();
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        return filmStorage.findPopularFilms(count, genreId, year);
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

    public List<Film> directorFilmsSortedByYear(Integer directorId) {
        return getFilmsByDirector(directorId).stream()
                .sorted(Comparator.comparingInt(f -> f.getReleaseDate().getYear()))
                .toList();
    }

    public List<Film> directorFilmsSortedByLikes(Integer directorId) {
        return getFilmsByDirector(directorId).stream()
                .sorted((f1, f2) ->
                        Integer.compare(likeStorage.getLikesCount(f2.getId()), likeStorage.getLikesCount(f1.getId())))
                .toList();
    }

    public List<Film> getFilmsByDirector(Integer directorId) {
        Director director = directorStorage.getDirectorById(directorId).orElseThrow(() ->
                new NoSuchElementException("Указанный режиссёр не найден"));
        return filmStorage.findAll().stream()
                .filter(film -> film.getDirectors().contains(director))
                .toList();
    }

    @Transactional
    public void deleteFilm(Long id) {
        Film film = filmStorage.findById(id.intValue())
                .orElse(null);
        if (film == null) {
            throw new NoSuchElementException("Фильм с id " + id + " не найден");
        }
        filmStorage.deleteFilmById(id);
    }

    public List<Film> getMutualFilmOfTwoUser(Long userId, Long friendId) {
        Set<Integer> userLikedFilms = likeStorage.getLikedFilmIds(userId.intValue());
        Set<Integer> otherUserLikedFilms = likeStorage.getLikedFilmIds(friendId.intValue());
        Set<Integer> intersection = new HashSet<>(userLikedFilms);
        intersection.retainAll(otherUserLikedFilms);

        return filmStorage.findFilmsByIds(intersection);
    }

    public List<Film> getSearch(String query, List<String> by) {
        if (by == null || by.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, Film> unique = new LinkedHashMap<>();
        for (String field : by) {
            List<Film> part = filmStorage.findByContains(field, query);
            for (Film f : part) {
                unique.putIfAbsent(f.getId(), f);
            }
        }

        List<Film> result = new ArrayList<>(unique.values());
        result.sort(
                Comparator.comparingInt((Film f) -> likeStorage.getLikesCount(f.getId()))
                        .reversed()
                        .thenComparingInt(Film::getId)
        );
        return result;
    }
}
