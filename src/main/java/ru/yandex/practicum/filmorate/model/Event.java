package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.Instant;

@Data
public class Event {
    private Long eventId;
    private Long timestamp;
    private Integer userId;
    private EventType eventType;
    private Operation operation;
    private Integer entityId;

    public Event() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public Event(Integer userId, EventType eventType, Operation operation, Integer entityId) {
        this();
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
    }

    public enum EventType {
        LIKE,
        REVIEW,
        FRIEND
    }

    public enum Operation {
        REMOVE,
        ADD,
        UPDATE
    }
}