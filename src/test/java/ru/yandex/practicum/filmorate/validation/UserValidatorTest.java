package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserValidatorTest {

	private User user;

	@BeforeEach
	void beforeEach() {
		user = new User();
		user.setEmail("user@example.com");
		user.setLogin("username");
		user.setName("User Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));
	}

	@Test
	void shouldThrowExceptionWhenEmailIsNull() {
		user.setEmail(null);
		ValidationException ex = assertThrows(ValidationException.class, () -> UserValidator.validate(user));
		assertEquals("Электронная почта не может быть пустой и должна содержать @", ex.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenEmailIsBlank() {
		user.setEmail("   ");
		assertThrows(ValidationException.class, () -> UserValidator.validate(user));
	}

	@Test
	void shouldThrowExceptionWhenEmailDoesNotContainAt() {
		user.setEmail("userexample.com");
		assertThrows(ValidationException.class, () -> UserValidator.validate(user));
	}

	@Test
	void shouldPassWhenEmailContainsAt() {
		user.setEmail("user@example.com");
		assertDoesNotThrow(() -> UserValidator.validate(user));
	}

	@Test
	void shouldThrowExceptionWhenLoginIsNull() {
		user.setLogin(null);
		assertThrows(ValidationException.class, () -> UserValidator.validate(user));
	}

	@Test
	void shouldThrowExceptionWhenLoginIsBlank() {
		user.setLogin("   ");
		assertThrows(ValidationException.class, () -> UserValidator.validate(user));
	}

	@Test
	void shouldThrowExceptionWhenLoginContainsSpace() {
		user.setLogin("user name");
		assertThrows(ValidationException.class, () -> UserValidator.validate(user));
	}

	@Test
	void shouldPassWhenLoginIsValid() {
		user.setLogin("username");
		assertDoesNotThrow(() -> UserValidator.validate(user));
	}

	@Test
	void shouldThrowExceptionWhenBirthdayIsInFuture() {
		user.setBirthday(LocalDate.now().plusDays(1));
		assertThrows(ValidationException.class, () -> UserValidator.validate(user));
	}

	@Test
	void shouldPassWhenBirthdayIsToday() {
		user.setBirthday(LocalDate.now());
		assertDoesNotThrow(() -> UserValidator.validate(user));
	}

	@Test
	void shouldPassWhenBirthdayIsNull() {
		user.setBirthday(null);
		assertDoesNotThrow(() -> UserValidator.validate(user));
	}

	@Test
	void shouldPassWithValidUser() {
		assertDoesNotThrow(() -> UserValidator.validate(user));
	}
}
