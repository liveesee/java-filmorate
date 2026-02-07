package ru.yandex.practicum.filmorate.storage.filmgenre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Set;

public interface FilmGenreStorage {
    Set<Genre> getGenres(Integer filmId);

    void setGenres(Integer filmId, Set<Genre> genres);
}
