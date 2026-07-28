package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmValidatorTest {

	private Film film;

	@BeforeEach
	void beforeEach() {
		film = new Film();
		film.setName("Test Film");
		film.setDescription("Test description");
		film.setReleaseDate(LocalDate.of(2020, 1, 1));
		film.setDuration(120);
	}

	@Test
	void shouldThrowExceptionWhenNameIsNull() {
		film.setName(null);
		ValidationException ex = assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
		assertEquals("Название фильма не может быть пустым", ex.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenNameIsBlank() {
		film.setName("   ");
		assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
	}

	@Test
	void shouldThrowExceptionWhenDescriptionExceeds200Chars() {
		film.setDescription("A".repeat(201));
		assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
	}

	@Test
	void shouldPassWhenDescriptionExactly200Chars() {
		film.setDescription("A".repeat(200));
		assertDoesNotThrow(() -> FilmValidator.validate(film));
	}

	@Test
	void shouldThrowExceptionWhenReleaseDateIsBefore1895December28() {
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
	}

	@Test
	void shouldPassWhenReleaseDateIs1895December28() {
		film.setReleaseDate(LocalDate.of(1895, 12, 28));
		assertDoesNotThrow(() -> FilmValidator.validate(film));
	}

	@Test
	void shouldThrowExceptionWhenDurationIsZero() {
		film.setDuration(0);
		assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
	}

	@Test
	void shouldThrowExceptionWhenDurationIsNegative() {
		film.setDuration(-1);
		assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
	}

	@Test
	void shouldPassWithValidFilm() {
		assertDoesNotThrow(() -> FilmValidator.validate(film));
	}

	@Test
	void shouldThrowExceptionWhenReleaseDateIsNull() {
		film.setReleaseDate(null);
		assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
	}
}
