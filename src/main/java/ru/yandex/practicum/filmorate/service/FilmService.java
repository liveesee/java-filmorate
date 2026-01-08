package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate FIRST_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
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
        log.info("Фильм успешно создан: ID={}, name={}", film.getId(), film.getName());
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Валидация не пройдена при обновлении фильма: ID должен быть указан");
            throw new ConditionNotMetException("ID должен быть указан");
        }
        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                log.warn("Валидация не пройдена при обновлении фильма: название должно быть указано");
                throw new ConditionNotMetException("Название должно быть указано");
            }
        }
        if (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
                log.warn("Валидация не пройдена при обновлении фильма: описание не должно быть длиннее {} символов", MAX_DESCRIPTION_LENGTH);
                throw new ConditionNotMetException("Описание не должно быть длиннее " + MAX_DESCRIPTION_LENGTH + " символов");
            }
        }
        if (newFilm.getReleaseDate() != null) {
            if (newFilm.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
                log.warn("Валидация не пройдена при обновлении фильма: дата релиза не должна быть раньше {}", FIRST_FILM_RELEASE_DATE);
                throw new ConditionNotMetException("Дата релиза не должна быть раньше " + FIRST_FILM_RELEASE_DATE);
            }
        }
        if (newFilm.getDuration() != null) {
            if (newFilm.getDuration() < 0) {
                log.warn("Валидация не пройдена при обновлении фильма: продолжительность должна быть положительным числом");
                throw new ConditionNotMetException("Продолжительность должна быть положительным числом");
            }
        }
        Film updatedFilm = filmStorage.update(newFilm);
        log.info("Фильм успешно обновлен: ID={}, name={}", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    public Film findById(Integer id) {
        return filmStorage.findById(id);
    }

    public void addLike(Integer filmId, Integer userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        if (film.getLikes().add(userId)) {
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        }
    }

    public void deleteLike(Integer filmId, Integer userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        if (film.getLikes().remove(userId)) {
            log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
        }
    }

    public List<Film> getTopPopular(int count) {
        if (count < 1) {
            throw new ConditionNotMetException("Количество фильмов в топе должно быть положительным");
        }
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
