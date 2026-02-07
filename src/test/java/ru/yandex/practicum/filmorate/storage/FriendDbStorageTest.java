package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.storage.friend.FriendDbStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FriendDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FriendDbStorageTest {

    private final FriendDbStorage friendStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void addGetDeleteFriend() {
        Integer userId = insertUser("u1@ex.com", "login1");
        Integer friendId = insertUser("u2@ex.com", "login2");

        friendStorage.addFriend(userId, friendId);
        friendStorage.addFriend(userId, friendId);

        Set<Integer> friends = friendStorage.getFriends(userId);
        assertThat(friends).containsExactly(friendId);

        friendStorage.deleteFriend(userId, friendId);
        Set<Integer> afterDelete = friendStorage.getFriends(userId);
        assertThat(afterDelete).isEmpty();
    }

    private Integer insertUser(String email, String login) {
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                email, login, login, LocalDate.of(1990, 1, 1));
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login = ?", Integer.class, login);
    }
}
