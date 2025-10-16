package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Event create(Event event) {
        String sql = "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"event_id"});
            stmt.setLong(1, event.getTimestamp());
            stmt.setInt(2, event.getUserId());
            stmt.setString(3, event.getEventType().toString());
            stmt.setString(4, event.getOperation().toString());
            stmt.setInt(5, event.getEntityId());
            return stmt;
        }, keyHolder);

        event.setEventId(keyHolder.getKey().longValue());
        return event;
    }

    @Override
    public List<Event> getEventsByUserId(Integer userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, this::mapRowToEvent, userId);
    }

    @Override
    public void addFriendEvent(Integer userId, Integer friendId, Event.Operation operation) {
        Event event = new Event(userId, Event.EventType.FRIEND, operation, friendId);
        create(event);
    }

    @Override
    public void addLikeEvent(Integer userId, Integer filmId, Event.Operation operation) {
        Event event = new Event(userId, Event.EventType.LIKE, operation, filmId);
        create(event);
    }

    @Override
    public void addReviewEvent(Integer userId, Integer reviewId, Event.Operation operation) {
        Event event = new Event(userId, Event.EventType.REVIEW, operation, reviewId);
        create(event);
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setUserId(rs.getInt("user_id"));
        event.setEventType(Event.EventType.valueOf(rs.getString("event_type")));
        event.setOperation(Event.Operation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getInt("entity_id"));
        return event;
    }
}