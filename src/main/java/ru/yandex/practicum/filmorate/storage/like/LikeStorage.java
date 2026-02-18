package ru.yandex.practicum.filmorate.storage.like;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface LikeStorage {
    void addLike(Integer filmId, Integer userId);

    void deleteLike(Integer filmId, Integer userId);

    Set<Integer> getLikes(Integer filmId);

    Map<Integer, Set<Integer>> getLikesByFilmIds(Collection<Integer> filmIds);
}
