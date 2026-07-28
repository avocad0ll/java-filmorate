package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserValidator {
	public static void validate(final User user) {
		if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
			throw new ValidationException("Электронная почта не может быть пустой и должна содержать @");
		}
		if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
			throw new ValidationException("Логин не может быть пустым и содержать пробелы");
		}
		if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
			throw new ValidationException("Дата рождения не может быть в будущем");
		}
	}
}
