package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void shouldSucceedWithValidData() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userController.create(user);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("test@example.com", created.getEmail());
        assertEquals("testlogin", created.getLogin());
    }

    @Test
    void shouldThrowExceptionWhenAllFieldsAreNull() {
        User user = new User();
        assertThrows(ConditionNotMetException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(ConditionNotMetException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAt() {
        User user = new User();
        user.setEmail("testexample.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(ConditionNotMetException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(ConditionNotMetException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpace() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(ConditionNotMetException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsNull() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(null);
        assertThrows(ConditionNotMetException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ConditionNotMetException.class, () -> userController.create(user));
    }

    @Test
    void shouldSucceedWhenBirthdayIsToday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now());
        User created = userController.create(user);
        assertNotNull(created);
        assertEquals(LocalDate.now(), created.getBirthday());
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsNull() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userController.create(user);
        assertEquals("testlogin", created.getName());
    }
}