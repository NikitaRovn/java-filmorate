package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {
    private final LikeStorage likeStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public RecommendationService(LikeStorage likeStorage, FilmStorage filmStorage, UserStorage userStorage) {
        this.likeStorage = likeStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getRecommendations(Integer userId) {
        log.info("Получение рекомендаций для пользователя с ID: {}", userId);

        if (!userStorage.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с id " + userId + " не найден");
        }

        Set<Integer> userLikes = likeStorage.getLikedFilmIds(userId);

        if (userLikes.isEmpty()) {
            log.info("У пользователя {} нет лайков, возвращаем пустой список рекомендаций", userId);
            return Collections.emptyList();
        }

        Integer mostSimilarUserId = findMostSimilarUser(userId, userLikes);

        if (mostSimilarUserId == null) {
            log.info("Не найден пользователь с похожими вкусами для пользователя {}", userId);
            return Collections.emptyList();
        }

        Set<Integer> similarUserLikes = likeStorage.getLikedFilmIds(mostSimilarUserId);
        Set<Integer> recommendedFilmIds = new HashSet<>(similarUserLikes);
        recommendedFilmIds.removeAll(userLikes);

        if (recommendedFilmIds.isEmpty()) {
            log.info("Нет фильмов для рекомендации пользователю {}", userId);
            return Collections.emptyList();
        }

        List<Film> recommendations = recommendedFilmIds.stream()
                .map(filmStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        log.info("Найдено {} рекомендаций для пользователя {}", recommendations.size(), userId);
        return recommendations;
    }

    private Integer findMostSimilarUser(Integer userId, Set<Integer> userLikes) {
        Map<Integer, Set<Integer>> userSimilarities = new HashMap<>();

        for (Integer filmId : userLikes) {
            Set<Integer> usersWhoLiked = likeStorage.getLikedUserIds(filmId);
            usersWhoLiked.remove(userId);

            for (Integer otherUserId : usersWhoLiked) {
                userSimilarities.computeIfAbsent(otherUserId, k -> new HashSet<>()).add(filmId);
            }
        }

        if (userSimilarities.isEmpty()) {
            return null;
        }

        return userSimilarities.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}