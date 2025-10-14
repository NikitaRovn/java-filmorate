package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    @Autowired
    public UserService(UserStorage userStorage, FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        friendshipStorage.addFriend(userId, friendId, FriendshipStatus.PENDING);
        log.info("Пользователь {} отправил запрос на дружбу пользователю {}", userId, friendId);
    }

    public void confirmFriend(Integer userId, Integer friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        friendshipStorage.updateFriendshipStatus(friendId, userId, FriendshipStatus.CONFIRMED);
        log.info("Пользователь {} подтвердил дружбу с пользователем {}", userId, friendId);
    }

    public void rejectFriend(Integer userId, Integer friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        friendshipStorage.removeFriend(friendId, userId);
        log.info("Пользователь {} отклонил запрос на дружбу от пользователя {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        friendshipStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил пользователя {} из друзей", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        validateUserExists(userId);

        return friendshipStorage.getFriendIds(userId).stream()
                .map(id -> userStorage.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<User> getConfirmedFriends(Integer userId) {
        validateUserExists(userId);

        return friendshipStorage.getFriends(userId).entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(entry -> userStorage.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<User> getPendingFriendRequests(Integer userId) {
        validateUserExists(userId);

        return friendshipStorage.getIncomingFriendRequests(userId).stream()
                .map(id -> userStorage.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        validateUserExists(userId);
        validateUserExists(otherUserId);

        Set<Integer> userFriends = friendshipStorage.getFriendIds(userId);
        Set<Integer> otherUserFriends = friendshipStorage.getFriendIds(otherUserId);

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(id -> userStorage.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public FriendshipStatus getFriendshipStatus(Integer userId, Integer friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        return friendshipStorage.getFriendshipStatus(userId, friendId);
    }

    private void validateUserExists(Integer userId) {
        if (!userStorage.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с id " + userId + " не найден");
        }
    }
}