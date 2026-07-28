package ru.yandex.practicum.filmorate.validation;

public class ValidationException extends RuntimeException {
	public ValidationException(final String message) {
		super(message);
	}
}
