package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    Integer id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Mpa mpa;
    Set<Genre> genres = new HashSet<>();
    Set<Integer> likes = new HashSet<>();
}
