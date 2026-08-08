package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {
	private final UserStorage userStorage;

	public UserService(@Qualifier("userDbStorage") final UserStorage userStorage) {
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
		userStorage.getById(userId);
		userStorage.getById(friendId);
		userStorage.addFriend(userId, friendId);
	}

	public void removeFriend(final int userId, final int friendId) {
		userStorage.getById(userId);
		userStorage.getById(friendId);
		userStorage.removeFriend(userId, friendId);
	}

	public List<User> getFriends(final int userId) {
		userStorage.getById(userId);
		return userStorage.getUserFriends(userId);
	}

	public List<User> getCommonFriends(final int userId, final int otherId) {
		userStorage.getById(userId);
		userStorage.getById(otherId);
		return userStorage.getCommonFriends(userId, otherId);
	}
}
