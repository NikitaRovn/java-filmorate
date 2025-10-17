package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    @Autowired
    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    @GetMapping("/{id}/feed")
    public List<Event> getUserFeed(@PathVariable Integer id) {
        log.info("Получен запрос на получение ленты событий пользователя с ID: {}", id);

        userService.validateUserExists(id);

        List<Event> events = eventService.getEventsByUserId(id);
        log.info("Возвращено {} событий для пользователя {}", events.size(), id);
        return events;
    }
}