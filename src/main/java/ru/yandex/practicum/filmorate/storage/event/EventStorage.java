package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;
import java.util.List;

public interface EventStorage {
    Event create(Event event);

    List<Event> getEventsByUserId(Integer userId);

    void addFriendEvent(Integer userId, Integer friendId, Event.Operation operation);

    void addLikeEvent(Integer userId, Integer filmId, Event.Operation operation);

    void addReviewEvent(Integer userId, Integer reviewId, Event.Operation operation);
}