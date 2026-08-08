package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
	private final Map<Integer, User> users = new HashMap<>();
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
}
