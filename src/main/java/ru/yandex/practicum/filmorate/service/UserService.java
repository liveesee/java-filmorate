package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена при создании пользователя: эмейл должен быть указан и содержать @");
            throw new ConditionNotMetException("Эмейл должен быть указан и содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена при создании пользователя: логин не может быть пустым или содержать пробелы");
            throw new ConditionNotMetException("Логин не может быть пустым или содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null) {
            log.warn("Валидация не пройдена при создании пользователя: дата рождения должна быть указана");
            throw new ConditionNotMetException("Дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена при создании пользователя: дата рождения не может быть в будущем");
            throw new ConditionNotMetException("Дата рождения не может быть в будущем");
        }
        log.info("Пользователь успешно создан: ID={}, login={}, email={}", user.getId(), user.getLogin(), user.getEmail());
        return userStorage.create(user);
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.warn("Валидация не пройдена при обновлении пользователя: ID не может быть пустым");
            throw new ConditionNotMetException("ID не может быть пустым");
        }
        if (userStorage.findById(newUser.getId()) == null) {
            log.warn("Ошибка при обновлении пользователя: пользователь с ID {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с ID " + newUser.getId() + " не найден");
        }
        if (newUser.getEmail() != null) {
            if (newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
                log.warn("Валидация не пройдена при обновлении пользователя: эмейл должен быть указан и содержать @");
                throw new ConditionNotMetException("Эмейл должен быть указан и содержать @");
            }
        }
        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
                log.warn("Валидация не пройдена при обновлении пользователя: логин не может быть пустым или содержать пробелы");
                throw new ConditionNotMetException("Логин не может быть пустым или содержать пробелы");
            }
        }
        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Валидация не пройдена при обновлении пользователя: дата рождения не может быть в будущем");
                throw new ConditionNotMetException("Дата рождения не может быть в будущем");
            }
        }
        User updatedUser = userStorage.update(newUser);
        log.info("Пользователь успешно обновлен: ID={}, login={}, email={}",
                updatedUser.getId(), updatedUser.getLogin(), updatedUser.getEmail());
        return updatedUser;
    }

    public User findById(Integer id) {
        return userStorage.findById(id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Нельзя добавить самого себя в друзья");
        }
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        userStorage.update(user);
        userStorage.update(friend);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        //if(!user.getFriends().contains(friendId)) {
        //    throw new ConditionNotMetException(user.getName() + " не друзья с " + friend.getName());
        //}
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.update(user);
        userStorage.update(friend);
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherId);
        Set<Integer> commonIds = user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .collect(Collectors.toSet());
        return commonIds.stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> findAllFriends(Integer userId) {
        User user = userStorage.findById(userId);
        return user.getFriends().stream().map(userStorage::findById).toList();
    }

}
