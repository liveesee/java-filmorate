package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate FIRST_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена при создании фильма: название должно быть указано");
            throw new ConditionNotMetException("Название должно быть указано");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Валидация не пройдена при создании фильма: описание не должно быть длиннее {} символов", MAX_DESCRIPTION_LENGTH);
            throw new ConditionNotMetException("Описание не должно быть длиннее " + MAX_DESCRIPTION_LENGTH + " символов");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Валидация не пройдена при создании фильма: дата релиза должна быть указана");
            throw new ConditionNotMetException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            log.warn("Валидация не пройдена при создании фильма: дата релиза не должна быть раньше {}", FIRST_FILM_RELEASE_DATE);
            throw new ConditionNotMetException("Дата релиза не должна быть раньше " + FIRST_FILM_RELEASE_DATE);
        }
        if (film.getDuration() == null) {
            log.warn("Валидация не пройдена при создании фильма: продолжительность должна быть указана");
            throw new ConditionNotMetException("Продолжительность должна быть указана");
        }
        if (film.getDuration() < 0) {
            log.warn("Валидация не пройдена при создании фильма: продолжительность должна быть положительным числом");
            throw new ConditionNotMetException("Продолжительность должна быть положительным числом");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан: ID={}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Валидация не пройдена при обновлении фильма: ID должен быть указан");
            throw new ConditionNotMetException("ID должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Ошибка при обновлении фильма: фильм с ID {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с ID " + newFilm.getId() + " не найден");
        }
        Film existingFilm = films.get(newFilm.getId());
        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                log.warn("Валидация не пройдена при обновлении фильма: название должно быть указано");
                throw new ConditionNotMetException("Название должно быть указано");
            }
            existingFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
                log.warn("Валидация не пройдена при обновлении фильма: описание не должно быть длиннее {} символов", MAX_DESCRIPTION_LENGTH);
                throw new ConditionNotMetException("Описание не должно быть длиннее " + MAX_DESCRIPTION_LENGTH + " символов");
            }
            existingFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            if (newFilm.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
                log.warn("Валидация не пройдена при обновлении фильма: дата релиза не должна быть раньше {}", FIRST_FILM_RELEASE_DATE);
                throw new ConditionNotMetException("Дата релиза не должна быть раньше " + FIRST_FILM_RELEASE_DATE);
            }
            existingFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            if (newFilm.getDuration() < 0) {
                log.warn("Валидация не пройдена при обновлении фильма: продолжительность должна быть положительным числом");
                throw new ConditionNotMetException("Продолжительность должна быть положительным числом");
            }
            existingFilm.setDuration(newFilm.getDuration());
        }
        log.info("Фильм успешно обновлен: ID={}, name={}", existingFilm.getId(), existingFilm.getName());
        return existingFilm;
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
