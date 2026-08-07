package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserService {
	private final UserStorage userStorage;

	public UserService(final UserStorage userStorage) {
		this.userStorage = userStorage;
	}

	public User add(final User user) {
		return userStorage.add(user);
	}

	public User update(final User user) {
		return userStorage.update(user);
	}

	public User getById(final int id) {
		return userStorage.getById(id);
	}

	public List<User> getAll() {
		return userStorage.getAll();
	}

	public void addFriend(final int userId, final int friendId) {
		User user = userStorage.getById(userId);
		User friend = userStorage.getById(friendId);
		if (friend.getFriends().containsKey(userId)) {
			user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
			friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
		} else {
			user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
		}
		log.info("Пользователи {} и {} теперь друзья", userId, friendId);
	}

	public void removeFriend(final int userId, final int friendId) {
		User user = userStorage.getById(userId);
		User friend = userStorage.getById(friendId);
		user.getFriends().remove(friendId);
		friend.getFriends().remove(userId);
		log.info("Пользователи {} и {} больше не друзья", userId, friendId);
	}

	public List<User> getFriends(final int userId) {
		User user = userStorage.getById(userId);
		List<User> friends = new ArrayList<>();
		for (int friendId : user.getFriends().keySet()) {
			friends.add(userStorage.getById(friendId));
		}
		return friends;
	}

	public List<User> getCommonFriends(final int userId, final int otherId) {
		User user = userStorage.getById(userId);
		User other = userStorage.getById(otherId);
		List<User> common = new ArrayList<>();
		for (int friendId : user.getFriends().keySet()) {
			if (other.getFriends().containsKey(friendId)) {
				common.add(userStorage.getById(friendId));
			}
		}
		return common;
	}
}
