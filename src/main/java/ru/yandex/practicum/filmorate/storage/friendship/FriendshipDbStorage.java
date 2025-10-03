package ru.yandex.practicum.filmorate.storage.friendship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FriendshipDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId, FriendshipStatus status) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, status.toString());
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void updateFriendshipStatus(Integer userId, Integer friendId, FriendshipStatus status) {
        String sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, status.toString(), userId, friendId);
    }

    @Override
    public boolean hasFriend(Integer userId, Integer friendId) {
        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public FriendshipStatus getFriendshipStatus(Integer userId, Integer friendId) {
        String sql = "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            String status = jdbcTemplate.queryForObject(sql, String.class, userId, friendId);
            return FriendshipStatus.valueOf(status);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map<Integer, FriendshipStatus> getFriends(Integer userId) {
        String sql = "SELECT friend_id, status FROM friendships WHERE user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToFriends, userId);
    }

    @Override
    public Set<Integer> getFriendIds(Integer userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("friend_id"), userId));
    }

    @Override
    public Set<Integer> getIncomingFriendRequests(Integer userId) {
        String sql = "SELECT user_id FROM friendships WHERE friend_id = ? AND status = 'PENDING'";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("user_id"), userId));
    }

    private Map<Integer, FriendshipStatus> mapRowToFriends(ResultSet rs) throws SQLException {
        Map<Integer, FriendshipStatus> friends = new HashMap<>();
        while (rs.next()) {
            Integer friendId = rs.getInt("friend_id");
            FriendshipStatus status = FriendshipStatus.valueOf(rs.getString("status"));
            friends.put(friendId, status);
        }
        return friends;
    }
}