package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
	private static final RowMapper<Film> FILM_MAPPER = (rs, rowNum) -> {
		Film film = new Film();
		film.setId(rs.getInt("id"));
		film.setName(rs.getString("name"));
		film.setDescription(rs.getString("description"));
		film.setReleaseDate(rs.getObject("release_date", LocalDate.class));
		film.setDuration(rs.getInt("duration"));
		int mpaId = rs.getInt("mpa_id");
		if (!rs.wasNull()) {
			Mpa mpa = new Mpa();
			mpa.setId(mpaId);
			mpa.setName(rs.getString("mpa_name"));
			film.setMpa(mpa);
		}
		return film;
	};

	private static final RowMapper<Genre> GENRE_MAPPER = (rs, rowNum) -> {
		Genre genre = new Genre();
		genre.setId(rs.getInt("id"));
		genre.setName(rs.getString("name"));
		return genre;
	};

	private static final RowMapper<Mpa> MPA_MAPPER = (rs, rowNum) -> {
		Mpa mpa = new Mpa();
		mpa.setId(rs.getInt("id"));
		mpa.setName(rs.getString("name"));
		return mpa;
	};

	private final JdbcTemplate jdbcTemplate;

	public FilmDbStorage(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Film add(final Film film) {
		FilmValidator.validate(film);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO films (name, description, release_date, duration, mpa_id) "
							+ "VALUES (?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, film.getName());
			ps.setString(2, film.getDescription());
			ps.setObject(3, film.getReleaseDate());
			ps.setInt(4, film.getDuration());
			if (film.getMpa() != null) {
				ps.setInt(5, film.getMpa().getId());
			} else {
				ps.setNull(5, Types.INTEGER);
			}
			return ps;
		}, keyHolder);
		film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
		insertGenres(film);
		loadGenres(film);
		loadMpa(film);
		log.info("Добавлен фильм с ID {}: {}", film.getId(), film.getName());
		return film;
	}

	@Override
	public Film update(final Film film) {
		FilmValidator.validate(film);
		if (film.getId() <= 0) {
			throw new NotFoundException("ID фильма должен быть положительным числом");
		}
		int rows = jdbcTemplate.update(
				"UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?",
				film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
				film.getMpa() != null ? film.getMpa().getId() : null, film.getId());
		if (rows == 0) {
			throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
		}
		jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
		insertGenres(film);
		loadGenres(film);
		log.info("Обновлён фильм с ID {}", film.getId());
		return film;
	}

	@Override
	public void delete(final int id) {
		int rows = jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
		if (rows == 0) {
			throw new NotFoundException("Фильм с ID " + id + " не найден");
		}
		log.info("Удалён фильм с ID {}", id);
	}

	@Override
	public Film getById(final int id) {
		List<Film> films = jdbcTemplate.query(
				"SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, r.name AS mpa_name "
						+ "FROM films f LEFT JOIN mpa_ratings r ON f.mpa_id = r.id WHERE f.id = ?",
				FILM_MAPPER, id);
		if (films.isEmpty()) {
			throw new NotFoundException("Фильм с ID " + id + " не найден");
		}
		loadGenres(films.get(0));
		return films.get(0);
	}

	@Override
	public List<Film> getAll() {
		List<Film> films = jdbcTemplate.query(
				"SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, r.name AS mpa_name "
						+ "FROM films f LEFT JOIN mpa_ratings r ON f.mpa_id = r.id ORDER BY f.id",
				FILM_MAPPER);
		loadGenresForFilms(films);
		return films;
	}

	@Override
	public void addLike(final int filmId, final int userId) {
		try {
			jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
			log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
		} catch (DataIntegrityViolationException e) {
			log.warn("Лайк пользователя {} фильму {} уже существует", userId, filmId);
		}
	}

	@Override
	public void removeLike(final int filmId, final int userId) {
		int rows = jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
		if (rows == 0) {
			throw new NotFoundException("Лайк пользователя " + userId + " у фильма " + filmId + " не найден");
		}
		log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
	}

	@Override
	public List<Film> getPopular(final int count) {
		List<Film> films = jdbcTemplate.query(
				"SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, r.name AS mpa_name, "
						+ "COUNT(l.user_id) AS likes_count "
						+ "FROM films f "
						+ "LEFT JOIN mpa_ratings r ON f.mpa_id = r.id "
						+ "LEFT JOIN likes l ON f.id = l.film_id "
						+ "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, r.name "
						+ "ORDER BY likes_count DESC LIMIT ?",
				FILM_MAPPER, count);
		loadGenresForFilms(films);
		return films;
	}

	private void insertGenres(final Film film) {
		if (film.getGenres() == null) {
			return;
		}
		Map<Integer, Genre> uniqueGenres = new LinkedHashMap<>();
		for (Genre genre : film.getGenres()) {
			if (genre != null && genre.getId() > 0) {
				uniqueGenres.putIfAbsent(genre.getId(), genre);
			}
		}
		if (uniqueGenres.isEmpty()) {
			return;
		}
		List<Integer> genreIds = new ArrayList<>(uniqueGenres.keySet());
		jdbcTemplate.batchUpdate(
				"INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(final PreparedStatement ps, final int i) throws SQLException {
						ps.setInt(1, film.getId());
						ps.setInt(2, genreIds.get(i));
					}

					@Override
					public int getBatchSize() {
						return genreIds.size();
					}
				});
	}

	private void loadGenres(final Film film) {
		List<Genre> genres = jdbcTemplate.query(
				"SELECT g.id, g.name FROM film_genres fg "
						+ "JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id = ? ORDER BY g.id",
				GENRE_MAPPER, film.getId());
		film.setGenres(new ArrayList<>(genres));
	}

	private void loadGenresForFilms(final List<Film> films) {
		if (films.isEmpty()) {
			return;
		}
		List<Integer> filmIds = new ArrayList<>();
		for (Film film : films) {
			filmIds.add(film.getId());
		}
		String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
		Map<Integer, List<Genre>> genresByFilm = new HashMap<>();
		jdbcTemplate.query(
				"SELECT fg.film_id, g.id, g.name FROM film_genres fg "
						+ "JOIN genres g ON fg.genre_id = g.id "
						+ "WHERE fg.film_id IN (" + placeholders + ") ORDER BY g.id",
				rs -> {
					int filmId = rs.getInt("film_id");
					genresByFilm.computeIfAbsent(filmId, k -> new ArrayList<>()).add(GENRE_MAPPER.mapRow(rs, 0));
				},
				filmIds.toArray());
		for (Film film : films) {
			film.setGenres(new ArrayList<>(genresByFilm.getOrDefault(film.getId(), Collections.emptyList())));
		}
	}

	private void loadMpa(final Film film) {
		if (film.getMpa() == null) {
			return;
		}
		Mpa mpa = jdbcTemplate.query("SELECT id, name FROM mpa_ratings WHERE id = ?",
				rs -> rs.next() ? MPA_MAPPER.mapRow(rs, 0) : null, film.getMpa().getId());
		film.setMpa(mpa);
	}
}
