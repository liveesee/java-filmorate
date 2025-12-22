package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if(user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена при создании пользователя: эмейл должен быть указан и содержать @");
            throw new ConditionNotMetException("Эмейл должен быть указан и содержать @");
        }
        if(user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена при создании пользователя: логин не может быть пустым или содержать пробелы");
            throw new ConditionNotMetException("Логин не может быть пустым или содержать пробелы");
        }
        if(user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if(user.getBirthday() == null) {
            log.warn("Валидация не пройдена при создании пользователя: дата рождения должна быть указана");
            throw new ConditionNotMetException("Дата рождения должна быть указана");
        }
        if(user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена при создании пользователя: дата рождения не может быть в будущем");
            throw new ConditionNotMetException("Дата рождения не может быть в будущем");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: ID={}, login={}, email={}", user.getId(), user.getLogin(), user.getEmail());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if(newUser.getId() == null) {
            log.warn("Валидация не пройдена при обновлении пользователя: ID не может быть пустым");
            throw new ConditionNotMetException("ID не может быть пустым");
        }
        if (!users.containsKey(newUser.getId())) {
            log.warn("Ошибка при обновлении пользователя: пользователь с ID {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с ID " + newUser.getId() + " не найден");
        }
        User existingUser = users.get(newUser.getId());
        if (newUser.getLogin() != null) {
            existingUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null) {
            existingUser.setEmail(newUser.getEmail());
        }
        if (newUser.getBirthday() != null) {
            existingUser.setBirthday(newUser.getBirthday());
        }
        log.info("Пользователь успешно обновлен: ID={}, login={}, email={}", existingUser.getId(), existingUser.getLogin(), existingUser.getEmail());
        return existingUser;
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
