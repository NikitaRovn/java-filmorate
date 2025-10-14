package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
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
    private final EventService eventService;

    @Autowired
    public UserService(UserStorage userStorage, FriendshipStorage friendshipStorage,
                       EventService eventService) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.eventService = eventService;
    }

    public void addFriend(Integer userId, Integer friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        friendshipStorage.addFriend(userId, friendId, FriendshipStatus.PENDING);
        eventService.addFriendEvent(userId, friendId, Event.Operation.ADD);
        log.info("Создано событие FRIEND: пользователь {} добавил в друзья пользователя {} (operation: ADD, entityId: {})",
                userId, friendId, friendId);
    }

    public void confirmFriend(Integer userId, Integer friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        friendshipStorage.updateFriendshipStatus(friendId, userId, FriendshipStatus.CONFIRMED);
        eventService.addFriendEvent(userId, friendId, Event.Operation.ADD);
        log.info("Создано событие FRIEND: пользователь {} подтвердил дружбу с пользователем {} (operation: ADD, entityId: {})",
                userId, friendId, friendId);
    }

    public void rejectFriend(Integer userId, Integer friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        friendshipStorage.removeFriend(friendId, userId);
        eventService.addFriendEvent(userId, friendId, Event.Operation.REMOVE);
        log.info("Создано событие FRIEND: пользователь {} отклонил заявку от пользователя {} (operation: REMOVE, entityId: {})",
                userId, friendId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        friendshipStorage.removeFriend(userId, friendId);
        eventService.addFriendEvent(userId, friendId, Event.Operation.REMOVE);
        log.info("Создано событие FRIEND: пользователь {} удалил из друзей пользователя {} (operation: REMOVE, entityId: {})",
                userId, friendId, friendId);
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

    public void validateUserExists(Integer userId) {
        if (!userStorage.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с id " + userId + " не найден");
        }
    }
}