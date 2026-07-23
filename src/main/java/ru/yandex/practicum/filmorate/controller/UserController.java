package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
	private final Map<Integer, User> users = new HashMap<>();
	private int nextId = 1;

	@PostMapping
	public User createUser(@RequestBody final User user) {
		UserValidator.validate(user);
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
		user.setId(nextId++);
		users.put(user.getId(), user);
		log.info("Создан пользователь: {}", user.getLogin());
		return user;
	}

	@PutMapping
	public User updateUser(@RequestBody final User user) {
		UserValidator.validate(user);
		if (user.getId() <= 0) {
			throw new ValidationException("ID пользователя должен быть положительным числом");
		}
		if (!users.containsKey(user.getId())) {
			throw new ValidationException("Пользователь с ID " + user.getId() + " не найден");
		}
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
		users.put(user.getId(), user);
		log.info("Обновлён пользователь: {}", user.getLogin());
		return user;
	}

	@GetMapping
	public List<User> getAllUsers() {
		log.info("Получен список всех пользователей, количество: {}", users.size());
		return new ArrayList<>(users.values());
	}
}
