package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {
	private final GenreDbStorage genreStorage;

	@Test
	void getAllReturnsSixGenresSortedById() {
		List<Genre> genres = genreStorage.getAll();

		assertThat(genres).hasSize(6);
		assertThat(genres).extracting(Genre::getId).containsExactly(1, 2, 3, 4, 5, 6);
		assertThat(genres.get(0).getName()).isEqualTo("Комедия");
	}

	@Test
	void getByIdReturnsGenre() {
		Genre genre = genreStorage.getById(3);

		assertThat(genre.getId()).isEqualTo(3);
		assertThat(genre.getName()).isEqualTo("Мультфильм");
	}

	@Test
	void getByIdThrowsWhenGenreNotFound() {
		assertThatThrownBy(() -> genreStorage.getById(99))
				.isInstanceOf(NotFoundException.class);
	}
}
