package ru.yandex.practicum.filmorate.storage.like;

import java.util.Set;

public interface LikeStorage {
    void addLike(Integer filmId, Integer userId);

    void removeLike(Integer filmId, Integer userId);

    boolean hasLike(Integer filmId, Integer userId);

    int getLikesCount(Integer filmId);

    Set<Integer> getLikedUserIds(Integer filmId);
}