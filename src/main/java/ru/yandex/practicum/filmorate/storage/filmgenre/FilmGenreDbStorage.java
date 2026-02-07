package ru.yandex.practicum.filmorate.storage.filmgenre;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Qualifier("filmGenreDbStorage")
public class FilmGenreDbStorage implements FilmGenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper = new GenreRowMapper();

    public FilmGenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Set<Genre> getGenres(Integer filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM genres AS g
                JOIN film_genres AS fg ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                """;
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper, filmId);
        return new HashSet<>(genres);
    }

    @Override
    public void setGenres(Integer filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        if (genres == null || genres.isEmpty()) {
            return;
        }
        List<Object[]> batch = genres.stream()
                .map(genre -> new Object[]{filmId, genre.getId()})
                .toList();
        jdbcTemplate.batchUpdate("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", batch);
    }
}
