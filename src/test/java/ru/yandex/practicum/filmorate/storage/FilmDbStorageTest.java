package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void createAndFindById() {
        ensureMpa(1, "G");
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa(1, null));

        Film created = filmStorage.create(film);

        Film found = filmStorage.findById(created.getId());
        assertThat(found.getName()).isEqualTo("Film");
        assertThat(found.getDuration()).isEqualTo(120);
        assertThat(found.getMpa()).isNotNull();
        assertThat(found.getMpa().getId()).isEqualTo(1);
        assertThat(found.getMpa().getName()).isEqualTo("G");
    }

    @Test
    void update() {
        ensureMpa(1, "G");
        ensureMpa(2, "PG");
        Integer id = insertFilm("Old", 90, 1);

        Film update = new Film();
        update.setId(id);
        update.setName("New");
        update.setDescription("New desc");
        update.setReleaseDate(LocalDate.of(2001, 2, 2));
        update.setDuration(95);
        update.setMpa(mpa(2, null));

        Film updated = filmStorage.update(update);

        Film found = filmStorage.findById(id);
        assertThat(updated.getId()).isEqualTo(id);
        assertThat(found.getName()).isEqualTo("New");
        assertThat(found.getDuration()).isEqualTo(95);
        assertThat(found.getMpa().getId()).isEqualTo(2);
        assertThat(found.getMpa().getName()).isEqualTo("PG");
    }

    @Test
    void findAll() {
        ensureMpa(1, "G");
        insertFilm("F1", 100, 1);
        insertFilm("F2", 110, 1);

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    private void ensureMpa(int id, String name) {
        jdbcTemplate.update("MERGE INTO mpa (id, name) KEY(id) VALUES (?, ?)", id, name);
    }

    private Integer insertFilm(String name, int duration, int mpaId) {
        jdbcTemplate.update("INSERT INTO films (name, description, releaseDate, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                name, "desc", LocalDate.of(2000, 1, 1), duration, mpaId);
        return jdbcTemplate.queryForObject("SELECT id FROM films WHERE name = ?", Integer.class, name);
    }

    private Mpa mpa(int id, String name) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        mpa.setName(name);
        return mpa;
    }
}
