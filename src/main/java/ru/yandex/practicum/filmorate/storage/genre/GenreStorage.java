package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Set;

public interface GenreStorage {
    Collection<Genre> findAll();

    Genre findById(Integer id);

    void validateIds(Set<Integer> ids);
}
