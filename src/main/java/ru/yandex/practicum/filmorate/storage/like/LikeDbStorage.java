package ru.yandex.practicum.filmorate.storage.like;

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
@Qualifier("likeDbStorage")
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbcTemplate;

    public LikeDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        String sql = "MERGE INTO likes (user_id, film_id) KEY(user_id, film_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, filmId);
    }

    @Override
    public void deleteLike(Integer filmId, Integer userId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(sql, userId, filmId);
    }

    @Override
    public Set<Integer> getLikes(Integer filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.queryForList(sql, Integer.class, filmId);
        return new HashSet<>(likes);
    }

    @Override
    public Map<Integer, Set<Integer>> getLikesByFilmIds(Collection<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT film_id, user_id FROM likes WHERE film_id IN (%s)".formatted(placeholders);
        Object[] args = filmIds.toArray();
        Map<Integer, Set<Integer>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Integer filmId = rs.getInt("film_id");
            Integer userId = rs.getInt("user_id");
            result.computeIfAbsent(filmId, id -> new HashSet<>()).add(userId);
        }, args);
        return result;
    }
}
