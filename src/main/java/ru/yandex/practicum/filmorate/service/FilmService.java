package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.filmgenre.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate FIRST_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("likeDbStorage") LikeStorage likeStorage,
                       @Qualifier("filmGenreDbStorage") FilmGenreStorage filmGenreStorage,
                       @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
                       @Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        if (likeStorage != null || filmGenreStorage != null) {
            films.forEach(this::fillRelations);
        }
        return films;
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
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            log.warn("Валидация не пройдена при создании фильма: рейтинг должен быть указан");
            throw new ConditionNotMetException("Рейтинг должен быть указан");
        }
        mpaStorage.findById(film.getMpa().getId());
        if (film.getDuration() < 0) {
            log.warn("Валидация не пройдена при создании фильма: продолжительность должна быть положительным числом");
            throw new ConditionNotMetException("Продолжительность должна быть положительным числом");
        }
        validateGenres(film);
        log.info("Фильм успешно создан: ID={}, name={}", film.getId(), film.getName());
        Film created = filmStorage.create(film);
        if (filmGenreStorage != null) {
            filmGenreStorage.setGenres(created.getId(), created.getGenres());
        }
        return created;
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Валидация не пройдена при обновлении фильма: ID должен быть указан");
            throw new ConditionNotMetException("ID должен быть указан");
        }
        if (newFilm.getMpa() == null || newFilm.getMpa().getId() == null) {
            log.warn("Валидация не пройдена при обновлении фильма: рейтинг должен быть указан");
            throw new ConditionNotMetException("Рейтинг должен быть указан");
        }
        mpaStorage.findById(newFilm.getMpa().getId());
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
        validateGenres(newFilm);
        Film updatedFilm = filmStorage.update(newFilm);
        if (filmGenreStorage != null) {
            filmGenreStorage.setGenres(updatedFilm.getId(), updatedFilm.getGenres());
        }
        log.info("Фильм успешно обновлен: ID={}, name={}", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    public Film findById(Integer id) {
        Film film = filmStorage.findById(id);
        fillRelations(film);
        return film;
    }

    public void addLike(Integer filmId, Integer userId) {
        userStorage.findById(userId);
        filmStorage.findById(filmId);
        if (likeStorage != null) {
            likeStorage.addLike(filmId, userId);
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
            return;
        }
        Film film = filmStorage.findById(filmId);
        if (film.getLikes().add(userId)) {
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        }
        filmStorage.update(film);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        userStorage.findById(userId);
        filmStorage.findById(filmId);
        if (likeStorage != null) {
            likeStorage.deleteLike(filmId, userId);
            log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
            return;
        }
        Film film = filmStorage.findById(filmId);
        if (film.getLikes().remove(userId)) {
            log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
        }
        filmStorage.update(film);
    }

    public List<Film> getTopPopular(int count) {
        if (count < 1) {
            throw new ConditionNotMetException("Количество фильмов в топе должно быть положительным");
        }
        return findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void fillRelations(Film film) {
        if (filmGenreStorage != null) {
            film.setGenres(filmGenreStorage.getGenres(film.getId()));
        }
        if (likeStorage != null) {
            film.setLikes(likeStorage.getLikes(film.getId()));
        }
    }

    private void validateGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        film.getGenres().forEach(genre -> {
            if (genre != null && genre.getId() != null) {
                genreStorage.findById(genre.getId());
            }
        });
    }
}
