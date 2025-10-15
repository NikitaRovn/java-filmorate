package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
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
    private final EventService eventService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       LikeStorage likeStorage, EventService eventService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeStorage;
        this.eventService = eventService;
    }

    public void addLike(Integer filmId, Integer userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);

        if (likeStorage.hasLike(filmId, userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            return;
        }

        likeStorage.addLike(filmId, userId);
        eventService.addLikeEvent(userId, filmId, Event.Operation.ADD);
        log.info("Создано событие LIKE: пользователь {} добавил лайк фильму {} (operation: ADD, entityId: {})",
                userId, filmId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);

        likeStorage.removeLike(filmId, userId);
        eventService.addLikeEvent(userId, filmId, Event.Operation.REMOVE);
        log.info("Создано событие LIKE: пользователь {} удалил лайк фильму {} (operation: REMOVE, entityId: {})",
                userId, filmId, filmId);
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

    @Transactional
    public void deleteFilm(Long id) {
        Film film = filmStorage.findById(id.intValue()).orElse(null);
        if (film == null) throw new NoSuchElementException("Фильм с id " + id + " не найден");
        filmStorage.deleteFilmById(id);
    }

    public List<Film> getMutualFilmOfTwoUser(Long userId, Long friendId) {
        Set<Integer> userLikedFilms = likeStorage.getLikedFilmIds(userId.intValue());
        Set<Integer> otherUserLikedFilms = likeStorage.getLikedFilmIds(friendId.intValue());
        Set<Integer> intersection = new HashSet<>(userLikedFilms);
        intersection.retainAll(otherUserLikedFilms);

        return filmStorage.findFilmsByIds(intersection);
    }
}