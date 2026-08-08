package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
	private final Map<Integer, User> users = new HashMap<>();
	private final Map<Integer, Map<Integer, FriendshipStatus>> friends = new HashMap<>();
	private int nextId = 1;

	@Override
	public User add(final User user) {
		UserValidator.validate(user);
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
		user.setId(nextId++);
		users.put(user.getId(), user);
		log.info("Создан пользователь: {}", user.getLogin());
		return user;
	}

	@Override
	public User update(final User user) {
		UserValidator.validate(user);
		if (user.getId() <= 0) {
			throw new NotFoundException("ID пользователя должен быть положительным числом");
		}
		if (!users.containsKey(user.getId())) {
			throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
		}
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
		users.put(user.getId(), user);
		log.info("Обновлён пользователь: {}", user.getLogin());
		return user;
	}

	@Override
	public void delete(final int id) {
		if (!users.containsKey(id)) {
			throw new NotFoundException("Пользователь с ID " + id + " не найден");
		}
		users.remove(id);
		log.info("Удалён пользователь с ID: {}", id);
	}

	@Override
	public User getById(final int id) {
		User user = users.get(id);
		if (user == null) {
			throw new NotFoundException("Пользователь с ID " + id + " не найден");
		}
		return user;
	}

	@Override
	public List<User> getAll() {
		log.info("Получен список всех пользователей, количество: {}", users.size());
		return new ArrayList<>(users.values());
	}

	@Override
	public void addFriend(final int userId, final int friendId) {
		getById(userId);
		getById(friendId);
		Map<Integer, FriendshipStatus> userFriends = friends.computeIfAbsent(userId, k -> new HashMap<>());
		Map<Integer, FriendshipStatus> friendFriends = friends.get(friendId);
		if (friendFriends != null && friendFriends.containsKey(userId)) {
			userFriends.put(friendId, FriendshipStatus.CONFIRMED);
			friendFriends.put(userId, FriendshipStatus.CONFIRMED);
		} else {
			userFriends.put(friendId, FriendshipStatus.UNCONFIRMED);
		}
		log.info("Пользователь {} добавил в друзья {}", userId, friendId);
	}

	@Override
	public void removeFriend(final int userId, final int friendId) {
		getById(userId);
		getById(friendId);
		Map<Integer, FriendshipStatus> userFriends = friends.get(userId);
		if (userFriends != null) {
			userFriends.remove(friendId);
		}
		log.info("Пользователь {} удалил из друзей {}", userId, friendId);
	}

	@Override
	public List<User> getUserFriends(final int userId) {
		getById(userId);
		List<User> result = new ArrayList<>();
		for (int friendId : friends.getOrDefault(userId, Map.of()).keySet()) {
			result.add(users.get(friendId));
		}
		return result;
	}

	@Override
	public List<User> getCommonFriends(final int userId, final int otherId) {
		getById(userId);
		getById(otherId);
		Set<Integer> first = new HashSet<>(friends.getOrDefault(userId, Map.of()).keySet());
		first.retainAll(friends.getOrDefault(otherId, Map.of()).keySet());
		List<User> result = new ArrayList<>();
		for (int friendId : first) {
			result.add(users.get(friendId));
		}
		return result;
	}
}
