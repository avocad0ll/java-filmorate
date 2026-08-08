package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
	private final Map<Integer, Film> films = new HashMap<>();
	private int nextId = 1;

	@Override
	public Film add(final Film film) {
		FilmValidator.validate(film);
		film.setId(nextId++);
		films.put(film.getId(), film);
		log.info("Добавлен фильм: {}", film.getName());
		return film;
	}

	@Override
	public Film update(final Film film) {
		FilmValidator.validate(film);
		if (film.getId() <= 0) {
			throw new NotFoundException("ID фильма должен быть положительным числом");
		}
		if (!films.containsKey(film.getId())) {
			throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
		}
		films.put(film.getId(), film);
		log.info("Обновлён фильм: {}", film.getName());
		return film;
	}

	@Override
	public void delete(final int id) {
		if (!films.containsKey(id)) {
			throw new NotFoundException("Фильм с ID " + id + " не найден");
		}
		films.remove(id);
		log.info("Удалён фильм с ID: {}", id);
	}

	@Override
	public Film getById(final int id) {
		Film film = films.get(id);
		if (film == null) {
			throw new NotFoundException("Фильм с ID " + id + " не найден");
		}
		return film;
	}

	@Override
	public List<Film> getAll() {
		log.info("Получен список всех фильмов, количество: {}", films.size());
		return new ArrayList<>(films.values());
	}
}
