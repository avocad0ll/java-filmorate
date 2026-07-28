package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmValidator {
	private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
	private static final int MAX_DESCRIPTION_LENGTH = 200;

	public static void validate(final Film film) {
		if (film.getName() == null || film.getName().isBlank()) {
			throw new ValidationException("Название фильма не может быть пустым");
		}
		if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
			throw new ValidationException("Описание фильма не может превышать 200 символов");
		}
		if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
			throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
		}
		if (film.getDuration() <= 0) {
			throw new ValidationException("Продолжительность фильма должна быть положительным числом");
		}
	}
}
