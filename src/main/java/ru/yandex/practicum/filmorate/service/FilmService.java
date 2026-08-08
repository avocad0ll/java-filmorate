package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
	private final FilmStorage filmStorage;
	private final UserStorage userStorage;

	public FilmService(@Qualifier("filmDbStorage") final FilmStorage filmStorage,
					   @Qualifier("userDbStorage") final UserStorage userStorage) {
		this.filmStorage = filmStorage;
		this.userStorage = userStorage;
	}

	public Film add(final Film film) {
		return filmStorage.add(film);
	}

	public Film update(final Film film) {
		return filmStorage.update(film);
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
