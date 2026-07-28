package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {
	private final FilmStorage filmStorage;
	private final UserStorage userStorage;

	public FilmService(final FilmStorage filmStorage, final UserStorage userStorage) {
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
		Film film = filmStorage.getById(filmId);
		userStorage.getById(userId);
		if (!film.getLikes().add(userId)) {
			log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
		} else {
			log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
		}
	}

	public void removeLike(final int filmId, final int userId) {
		Film film = filmStorage.getById(filmId);
		userStorage.getById(userId);
		if (!film.getLikes().remove(userId)) {
			throw new NotFoundException("Лайк пользователя " + userId + " у фильма " + filmId + " не найден");
		}
		log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
	}

	public List<Film> getPopular(final int count) {
		return filmStorage.getAll().stream()
				.sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
				.limit(count)
				.toList();
	}
}
