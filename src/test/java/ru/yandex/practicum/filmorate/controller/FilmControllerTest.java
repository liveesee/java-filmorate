package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        FilmService filmService = new FilmService(filmStorage);
        filmController = new FilmController(filmService);
    }

    @Test
    void shouldSucceedWithValidData() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = filmController.create(film);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
        assertEquals(120, created.getDuration());
    }

    @Test
    void shouldThrowExceptionWhenAllFieldsAreNull() {
        Film film = new Film();
        assertThrows(ConditionNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        assertThrows(ConditionNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        assertThrows(ConditionNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void shouldSucceedWhenDescriptionIsExactly200Characters() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = filmController.create(film);
        assertNotNull(created);
        assertEquals(200, created.getDescription().length());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsLongerThan200Characters() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        assertThrows(ConditionNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsNull() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(null);
        film.setDuration(120);
        assertThrows(ConditionNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeMinimum() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);
        assertThrows(ConditionNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void shouldSucceedWhenReleaseDateIsMinimum() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);
        Film created = filmController.create(film);
        assertNotNull(created);
        assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNull() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(null);
        assertThrows(ConditionNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);
        assertThrows(ConditionNotMetException.class, () -> filmController.create(film));
    }
}
