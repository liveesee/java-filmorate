package ru.yandex.practicum.filmorate.storage.friend;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Qualifier("friendDbStorage")
public class FriendDbStorage implements FriendStorage {

    private final JdbcTemplate jdbcTemplate;

    public FriendDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        String sql = "MERGE INTO friends (user_id, friend_id) KEY(user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Set<Integer> getFriends(Integer userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        List<Integer> friends = jdbcTemplate.queryForList(sql, Integer.class, userId);
        return new HashSet<>(friends);
    }

    @Override
    public Map<Integer, Set<Integer>> getFriendsByUserIds(Collection<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = userIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT user_id, friend_id FROM friends WHERE user_id IN (%s)".formatted(placeholders);
        Map<Integer, Set<Integer>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Integer userId = rs.getInt("user_id");
            Integer friendId = rs.getInt("friend_id");
            result.computeIfAbsent(userId, id -> new HashSet<>()).add(friendId);
        }, userIds.toArray());
        return result;
    }

    @Override
    public Set<Integer> getCommonFriendIds(Integer userId, Integer otherId) {
        String sql = """
                SELECT f1.friend_id
                FROM friends AS f1
                JOIN friends AS f2 ON f1.friend_id = f2.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                """;
        List<Integer> common = jdbcTemplate.queryForList(sql, Integer.class, userId, otherId);
        return new HashSet<>(common);
    }
}
