package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FriendStorage friendStorage;

    public Collection<User> findAll() {
        Collection<User> users = userStorage.findAll();
        if (friendStorage != null) {
            users.forEach(this::fillFriends);
        }
        return users;
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
        User user = userStorage.findById(id);
        if (friendStorage != null) {
            fillFriends(user);
        }
        return user;
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Нельзя добавить самого себя в друзья");
        }
        userStorage.findById(userId);
        userStorage.findById(friendId);
        friendStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        userStorage.findById(userId);
        userStorage.findById(friendId);
        friendStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = findById(userId);
        User other = findById(otherId);
        Set<Integer> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(other.getFriends());
        return commonIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> findAllFriends(Integer userId) {
        User user = findById(userId);
        return user.getFriends().stream().map(this::findById).toList();
    }

    private void fillFriends(User user) {
        user.setFriends(friendStorage.getFriends(user.getId()));
    }
}
