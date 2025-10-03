package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на список всех пользователей. Текущее количество: {}", userStorage.findAll().size());
        return userStorage.findAll();
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable Integer id) {
        log.info("Получен запрос на получение пользователя с ID: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + id + " не найден"));
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        setUserNameFromLoginIfEmpty(user);
        User createdUser = userStorage.create(user);
        log.info("Добавлен новый пользователь: {}", createdUser);
        return createdUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (!userStorage.existsById(user.getId())) {
            log.warn("Попытка обновления несуществующего пользователя с ID: {}", user.getId());
            throw new NoSuchElementException("Пользователь с id " + user.getId() + " не найден");
        }
        setUserNameFromLoginIfEmpty(user);
        User updatedUser = userStorage.update(user);
        log.info("Обновлен пользователь: {}", updatedUser);
        return updatedUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.addFriend(id, friendId);
    }

    @PutMapping("/{id}/friends/confirm/{friendId}")
    public void confirmFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.confirmFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/reject/{friendId}")
    public void rejectFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.rejectFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Integer id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/confirmed")
    public List<User> getConfirmedFriends(@PathVariable Integer id) {
        return userService.getConfirmedFriends(id);
    }

    @GetMapping("/{id}/friends/pending")
    public List<User> getPendingFriendRequests(@PathVariable Integer id) {
        return userService.getPendingFriendRequests(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/friends/status/{friendId}")
    public FriendshipStatus getFriendshipStatus(@PathVariable Integer id, @PathVariable Integer friendId) {
        return userService.getFriendshipStatus(id, friendId);
    }

    private void setUserNameFromLoginIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Для пользователя {} установлено имя из логина", user.getLogin());
        }
    }
}