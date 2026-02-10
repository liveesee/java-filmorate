package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Collection<Film> findAll();

    Collection<Film> findTopPopular(int count);

    Film create(Film film);

    Film update(Film film);

    Film findById(Integer id);
}
