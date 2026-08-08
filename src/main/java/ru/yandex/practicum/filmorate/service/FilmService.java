package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
	private final FilmStorage filmStorage;
	private final UserStorage userStorage;
	private final GenreStorage genreStorage;
	private final MpaStorage mpaStorage;

	public FilmService(@Qualifier("filmDbStorage") final FilmStorage filmStorage,
					   @Qualifier("userDbStorage") final UserStorage userStorage,
					   final GenreStorage genreStorage,
					   final MpaStorage mpaStorage) {
		this.filmStorage = filmStorage;
		this.userStorage = userStorage;
		this.genreStorage = genreStorage;
		this.mpaStorage = mpaStorage;
	}

	public Film add(final Film film) {
		validateReferences(film);
		return filmStorage.add(film);
	}

	public Film update(final Film film) {
		validateReferences(film);
		return filmStorage.update(film);
	}

	private void validateReferences(final Film film) {
		if (film.getMpa() != null) {
			mpaStorage.getById(film.getMpa().getId());
		}
		if (film.getGenres() != null) {
			for (Genre genre : film.getGenres()) {
				if (genre != null) {
					genreStorage.getById(genre.getId());
				}
			}
		}
	}

	public Film getById(final int id) {
		return filmStorage.getById(id);
	}

	public List<Film> getAll() {
		return filmStorage.getAll();
	}

	public void addLike(final int filmId, final int userId) {
		filmStorage.getById(filmId);
		userStorage.getById(userId);
		filmStorage.addLike(filmId, userId);
	}

	public void removeLike(final int filmId, final int userId) {
		filmStorage.getById(filmId);
		userStorage.getById(userId);
		filmStorage.removeLike(filmId, userId);
	}

	public List<Film> getPopular(final int count) {
		log.info("Запрос популярных фильмов, количество: {}", count);
		return filmStorage.getPopular(count);
	}
}
