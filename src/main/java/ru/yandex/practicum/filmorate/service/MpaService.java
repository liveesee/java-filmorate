package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.Comparator;

@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    public MpaService(@Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<Mpa> findAll() {
        return mpaStorage.findAll().stream()
                .sorted(Comparator.comparing(Mpa::getId))
                .toList();
    }

    public Mpa findById(Integer id) {
        return mpaStorage.findById(id);
    }
}
