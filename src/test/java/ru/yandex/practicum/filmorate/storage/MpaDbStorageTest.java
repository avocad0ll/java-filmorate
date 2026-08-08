package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {
	private final MpaDbStorage mpaStorage;

	@Test
	void getAllReturnsFiveRatingsSortedById() {
		List<Mpa> ratings = mpaStorage.getAll();

		assertThat(ratings).hasSize(5);
		assertThat(ratings).extracting(Mpa::getId).containsExactly(1, 2, 3, 4, 5);
		assertThat(ratings.get(0).getName()).isEqualTo("G");
	}

	@Test
	void getByIdReturnsRating() {
		Mpa mpa = mpaStorage.getById(3);

		assertThat(mpa.getId()).isEqualTo(3);
		assertThat(mpa.getName()).isEqualTo("PG-13");
	}

	@Test
	void getByIdThrowsWhenRatingNotFound() {
		assertThatThrownBy(() -> mpaStorage.getById(99))
				.isInstanceOf(NotFoundException.class);
	}
}
