package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import java.util.Map;
import java.util.Set;

public interface FriendshipStorage {
    void addFriend(Integer userId, Integer friendId, FriendshipStatus status);

    void removeFriend(Integer userId, Integer friendId);

    void updateFriendshipStatus(Integer userId, Integer friendId, FriendshipStatus status);

    boolean hasFriend(Integer userId, Integer friendId);

    FriendshipStatus getFriendshipStatus(Integer userId, Integer friendId);

    Map<Integer, FriendshipStatus> getFriends(Integer userId);

    Set<Integer> getFriendIds(Integer userId);

    Set<Integer> getIncomingFriendRequests(Integer userId);
}