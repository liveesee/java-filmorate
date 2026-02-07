package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.filmgenre.FilmGenreDbStorage;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmGenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmGenreDbStorageTest {

    private final FilmGenreDbStorage filmGenreStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void setAndGetGenres() {
        ensureMpa(1, "G");
        Integer filmId = insertFilm("Film", 1);

        Genre g1 = genre(1);
        Genre g2 = genre(2);
        filmGenreStorage.setGenres(filmId, Set.of(g1, g2));

        Set<Genre> genres = filmGenreStorage.getGenres(filmId);
        Set<Integer> ids = genres.stream().map(Genre::getId).collect(Collectors.toSet());
        assertThat(ids).containsExactlyInAnyOrder(1, 2);

        filmGenreStorage.setGenres(filmId, Set.of());
        Set<Genre> afterClear = filmGenreStorage.getGenres(filmId);
        assertThat(afterClear).isEmpty();
    }

    private Integer insertFilm(String name, int mpaId) {
        jdbcTemplate.update("INSERT INTO films (name, description, releaseDate, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                name, "desc", LocalDate.of(2000, 1, 1), 100, mpaId);
        return jdbcTemplate.queryForObject("SELECT id FROM films WHERE name = ?", Integer.class, name);
    }

    private void ensureMpa(int id, String name) {
        jdbcTemplate.update("MERGE INTO mpa (id, name) KEY(id) VALUES (?, ?)", id, name);
    }

    private Genre genre(int id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }
}
