package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
	private final FilmDbStorage filmStorage;
	private final UserDbStorage userStorage;
	private final JdbcTemplate jdbcTemplate;

	private Film createFilm() {
		Film film = new Film();
		film.setName("Matrix");
		film.setDescription("Test film");
		film.setReleaseDate(LocalDate.of(1999, 3, 31));
		film.setDuration(136);
		Mpa mpa = new Mpa();
		mpa.setId(1);
		film.setMpa(mpa);
		Genre genre = new Genre();
		genre.setId(1);
		film.setGenres(List.of(genre));
		return film;
	}

	private User createUser(final String email, final String login) {
		User user = new User();
		user.setEmail(email);
		user.setLogin(login);
		user.setName(login);
		user.setBirthday(LocalDate.of(1990, 1, 1));
		return user;
	}

	@Test
	void addReturnsFilmWithGenresAndMpa() {
		Film film = filmStorage.add(createFilm());

		assertThat(film.getId()).isPositive();
		Film saved = filmStorage.getById(film.getId());
		assertThat(saved.getName()).isEqualTo("Matrix");
		assertThat(saved.getDuration()).isEqualTo(136);
		assertThat(saved.getMpa()).isNotNull();
		assertThat(saved.getMpa().getId()).isEqualTo(1);
		assertThat(saved.getMpa().getName()).isEqualTo("G");
		assertThat(saved.getGenres()).hasSize(1);
		assertThat(saved.getGenres().get(0).getName()).isEqualTo("Комедия");
	}

	@Test
	void addReturnsFilmWithoutGenres() {
		Film film = createFilm();
		film.setGenres(null);

		Film saved = filmStorage.add(film);

		assertThat(saved.getGenres()).isEmpty();
	}

	@Test
	void getByIdThrowsWhenFilmNotFound() {
		assertThatThrownBy(() -> filmStorage.getById(999))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void updateChangesFilmData() {
		Film film = filmStorage.add(createFilm());
		film.setName("Matrix Reloaded");
		film.setDuration(138);
		Genre genre = new Genre();
		genre.setId(2);
		film.setGenres(List.of(genre));

		Film updated = filmStorage.update(film);

		assertThat(updated.getName()).isEqualTo("Matrix Reloaded");
		assertThat(updated.getDuration()).isEqualTo(138);
		assertThat(updated.getGenres()).hasSize(1);
		assertThat(updated.getGenres().get(0).getId()).isEqualTo(2);
	}

	@Test
	void updateThrowsWhenFilmNotFound() {
		Film film = createFilm();
		film.setId(999);

		assertThatThrownBy(() -> filmStorage.update(film))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void getAllReturnsAllFilms() {
		filmStorage.add(createFilm());
		filmStorage.add(createFilm());

		List<Film> films = filmStorage.getAll();

		assertThat(films).hasSize(2);
		assertThat(films).extracting(Film::getName).containsExactly("Matrix", "Matrix");
	}

	@Test
	void deleteRemovesFilm() {
		Film film = filmStorage.add(createFilm());

		filmStorage.delete(film.getId());

		assertThatThrownBy(() -> filmStorage.getById(film.getId()))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void addLikeAndGetPopularOrdersByLikes() {
		Film first = filmStorage.add(createFilm());
		Film second = filmStorage.add(createFilm());
		Film third = filmStorage.add(createFilm());
		int user1 = userStorage.add(createUser("a@mail.ru", "a")).getId();
		int user2 = userStorage.add(createUser("b@mail.ru", "b")).getId();

		filmStorage.addLike(second.getId(), user1);
		filmStorage.addLike(second.getId(), user2);
		filmStorage.addLike(first.getId(), user1);

		List<Film> popular = filmStorage.getPopular(10);

		assertThat(popular).extracting(Film::getId).containsExactly(
				second.getId(), first.getId(), third.getId());
	}

	@Test
	void getPopularRespectsLimit() {
		Film first = filmStorage.add(createFilm());
		Film second = filmStorage.add(createFilm());
		int user1 = userStorage.add(createUser("a@mail.ru", "a")).getId();
		filmStorage.addLike(first.getId(), user1);

		List<Film> popular = filmStorage.getPopular(1);

		assertThat(popular).extracting(Film::getId).containsExactly(first.getId());
		assertThat(popular).doesNotContain(second);
	}

	@Test
	void removeLikeRemovesUsersLike() {
		Film film = filmStorage.add(createFilm());
		int user = userStorage.add(createUser("a@mail.ru", "a")).getId();
		filmStorage.addLike(film.getId(), user);

		filmStorage.removeLike(film.getId(), user);

		Integer likes = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM likes WHERE film_id = ?", Integer.class, film.getId());
		assertThat(likes).isZero();
	}
}
