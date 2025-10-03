package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FriendshipDbStorage.class, UserDbStorage.class})
class FriendshipDbStorageTest {

    private final FriendshipDbStorage friendshipStorage;
    private final UserDbStorage userStorage;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = createUser("user1@example.com", "user1", "User One");
        user2 = createUser("user2@example.com", "user2", "User Two");
        user3 = createUser("user3@example.com", "user3", "User Three");
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    @Test
    void testAddFriend() {
        friendshipStorage.addFriend(user1.getId(), user2.getId(), FriendshipStatus.PENDING);

        boolean hasFriend = friendshipStorage.hasFriend(user1.getId(), user2.getId());
        FriendshipStatus status = friendshipStorage.getFriendshipStatus(user1.getId(), user2.getId());

        assertThat(hasFriend).isTrue();
        assertThat(status).isEqualTo(FriendshipStatus.PENDING);
    }

    @Test
    void testRemoveFriend() {
        friendshipStorage.addFriend(user1.getId(), user2.getId(), FriendshipStatus.PENDING);
        friendshipStorage.removeFriend(user1.getId(), user2.getId());

        boolean hasFriend = friendshipStorage.hasFriend(user1.getId(), user2.getId());

        assertThat(hasFriend).isFalse();
    }

    @Test
    void testUpdateFriendshipStatus() {
        friendshipStorage.addFriend(user1.getId(), user2.getId(), FriendshipStatus.PENDING);
        friendshipStorage.updateFriendshipStatus(user1.getId(), user2.getId(), FriendshipStatus.CONFIRMED);

        FriendshipStatus status = friendshipStorage.getFriendshipStatus(user1.getId(), user2.getId());

        assertThat(status).isEqualTo(FriendshipStatus.CONFIRMED);
    }

    @Test
    void testGetFriends() {
        friendshipStorage.addFriend(user1.getId(), user2.getId(), FriendshipStatus.PENDING);
        friendshipStorage.addFriend(user1.getId(), user3.getId(), FriendshipStatus.CONFIRMED);

        Map<Integer, FriendshipStatus> friends = friendshipStorage.getFriends(user1.getId());

        assertThat(friends).hasSize(2);
        assertThat(friends).containsKeys(user2.getId(), user3.getId());
        assertThat(friends.get(user2.getId())).isEqualTo(FriendshipStatus.PENDING);
        assertThat(friends.get(user3.getId())).isEqualTo(FriendshipStatus.CONFIRMED);
    }

    @Test
    void testGetFriendIds() {
        friendshipStorage.addFriend(user1.getId(), user2.getId(), FriendshipStatus.PENDING);
        friendshipStorage.addFriend(user1.getId(), user3.getId(), FriendshipStatus.CONFIRMED);

        Set<Integer> friendIds = friendshipStorage.getFriendIds(user1.getId());

        assertThat(friendIds).hasSize(2);
        assertThat(friendIds).contains(user2.getId(), user3.getId());
    }

    @Test
    void testHasFriend_False() {
        boolean hasFriend = friendshipStorage.hasFriend(user1.getId(), user2.getId());

        assertThat(hasFriend).isFalse();
    }

    @Test
    void testGetFriendshipStatus_NotFound() {
        FriendshipStatus status = friendshipStorage.getFriendshipStatus(user1.getId(), user2.getId());

        assertThat(status).isNull();
    }
}