package ru.yandex.practicum.filmorate.storage.filmgenre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface FilmGenreStorage {
    Set<Genre> getGenres(Integer filmId);

    Map<Integer, Set<Genre>> getGenresByFilmIds(Collection<Integer> filmIds);

    void setGenres(Integer filmId, Set<Genre> genres);
}
