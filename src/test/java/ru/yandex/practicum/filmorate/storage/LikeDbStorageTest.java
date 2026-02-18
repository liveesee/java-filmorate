package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(LikeDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class LikeDbStorageTest {

    private final LikeDbStorage likeStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void addGetDeleteLike() {
        ensureMpa(1, "G");
        Integer userId = insertUser("u@ex.com", "login");
        Integer filmId = insertFilm("Film", 1);

        likeStorage.addLike(filmId, userId);
        likeStorage.addLike(filmId, userId);

        Set<Integer> likes = likeStorage.getLikes(filmId);
        assertThat(likes).containsExactly(userId);

        likeStorage.deleteLike(filmId, userId);
        Set<Integer> afterDelete = likeStorage.getLikes(filmId);
        assertThat(afterDelete).isEmpty();
    }

    private Integer insertUser(String email, String login) {
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                email, login, login, LocalDate.of(1990, 1, 1));
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login = ?", Integer.class, login);
    }

    private Integer insertFilm(String name, int mpaId) {
        jdbcTemplate.update("INSERT INTO films (name, description, releaseDate, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                name, "desc", LocalDate.of(2000, 1, 1), 100, mpaId);
        return jdbcTemplate.queryForObject("SELECT id FROM films WHERE name = ?", Integer.class, name);
    }

    private void ensureMpa(int id, String name) {
        jdbcTemplate.update("MERGE INTO mpa (id, name) KEY(id) VALUES (?, ?)", id, name);
    }
}
