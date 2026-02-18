package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Qualifier("genreDbStorage")
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper = new GenreRowMapper();

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT id, name FROM genres";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    @Override
    public Genre findById(Integer id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper, id);
        if (genres.isEmpty()) {
            log.warn("Жанр с ID {} не найден", id);
            throw new NotFoundException("Жанр с ID - " + id + " не найден");
        }
        return genres.get(0);
    }

    @Override
    public void validateIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT id FROM genres WHERE id IN (%s)".formatted(placeholders);
        List<Integer> found = jdbcTemplate.queryForList(sql, Integer.class, ids.toArray());
        Set<Integer> missing = new HashSet<>(ids);
        missing.removeAll(found);
        if (!missing.isEmpty()) {
            Integer missingId = missing.iterator().next();
            log.warn("Жанр с ID {} не найден", missingId);
            throw new NotFoundException("Жанр с ID - " + missingId + " не найден");
        }
    }
}
