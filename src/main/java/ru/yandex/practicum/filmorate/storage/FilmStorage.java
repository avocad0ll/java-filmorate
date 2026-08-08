package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
	Film add(Film film);

	Film update(Film film);

	void delete(int id);

	Film getById(int id);

	List<Film> getAll();

	void addLike(int filmId, int userId);

	void removeLike(int filmId, int userId);

	List<Film> getPopular(int count);
}
