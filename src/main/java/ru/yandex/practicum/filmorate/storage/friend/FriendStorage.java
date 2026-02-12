package ru.yandex.practicum.filmorate.storage.friend;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface FriendStorage {
    void addFriend(Integer userId, Integer friendId);

    void deleteFriend(Integer userId, Integer friendId);

    Set<Integer> getFriends(Integer userId);

    Map<Integer, Set<Integer>> getFriendsByUserIds(Collection<Integer> userIds);

    Set<Integer> getCommonFriendIds(Integer userId, Integer otherId);
}
