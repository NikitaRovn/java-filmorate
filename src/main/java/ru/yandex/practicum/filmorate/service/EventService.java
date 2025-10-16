package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import java.util.List;

@Service
public class EventService {

    private final EventStorage eventStorage;

    @Autowired
    public EventService(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    public List<Event> getEventsByUserId(Integer userId) {
        return eventStorage.getEventsByUserId(userId);
    }

    public void addFriendEvent(Integer userId, Integer friendId, Event.Operation operation) {
        eventStorage.addFriendEvent(userId, friendId, operation);
    }

    public void addLikeEvent(Integer userId, Integer filmId, Event.Operation operation) {
        eventStorage.addLikeEvent(userId, filmId, operation);
    }

    public void addReviewEvent(Integer userId, Integer reviewId, Event.Operation operation) {
        eventStorage.addReviewEvent(userId, reviewId, operation);
    }
}