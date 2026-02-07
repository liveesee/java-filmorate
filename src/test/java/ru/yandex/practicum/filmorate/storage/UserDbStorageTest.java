package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void createAndFindById() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        User found = userStorage.findById(created.getId());
        assertThat(found.getId()).isNotNull();
        assertThat(found.getEmail()).isEqualTo("a@b.com");
        assertThat(found.getLogin()).isEqualTo("login");
        assertThat(found.getName()).isEqualTo("Name");
        assertThat(found.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void update() {
        Integer id = insertUser("u@ex.com", "login1", "Old", LocalDate.of(1985, 5, 5));
        User update = new User();
        update.setId(id);
        update.setEmail("new@ex.com");
        update.setLogin("login2");
        update.setName("New");
        update.setBirthday(LocalDate.of(1999, 9, 9));

        User updated = userStorage.update(update);

        User found = userStorage.findById(id);
        assertThat(updated.getId()).isEqualTo(id);
        assertThat(found.getEmail()).isEqualTo("new@ex.com");
        assertThat(found.getLogin()).isEqualTo("login2");
        assertThat(found.getName()).isEqualTo("New");
        assertThat(found.getBirthday()).isEqualTo(LocalDate.of(1999, 9, 9));
    }

    @Test
    void findAll() {
        insertUser("u1@ex.com", "login1", "User1", LocalDate.of(1980, 1, 1));
        insertUser("u2@ex.com", "login2", "User2", LocalDate.of(1981, 2, 2));

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    private Integer insertUser(String email, String login, String name, LocalDate birthday) {
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                email, login, name, birthday);
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login = ?", Integer.class, login);
    }
}
