package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> handleValidation(final ValidationException e) {
		return Map.of("error", e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Map<String, String> handleNotFound(final NotFoundException e) {
		return Map.of("error", e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleThrowable(final Throwable e) {
		return Map.of("error", "Произошла внутренняя ошибка сервера");
	}
}
