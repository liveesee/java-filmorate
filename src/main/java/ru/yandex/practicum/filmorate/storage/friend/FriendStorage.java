package ru.yandex.practicum.filmorate.storage.friend;

import java.util.Set;

public interface FriendStorage {
    void addFriend(Integer userId, Integer friendId);

    void deleteFriend(Integer userId, Integer friendId);

    Set<Integer> getFriends(Integer userId);

    Set<Integer> getCommonFriendIds(Integer userId, Integer otherId);
}
