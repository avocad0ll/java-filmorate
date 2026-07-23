package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidator;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
	private final Map<Integer, Film> films = new HashMap<>();
	private int nextId = 1;

	@PostMapping
	public Film addFilm(@RequestBody final Film film) {
		FilmValidator.validate(film);
		film.setId(nextId++);
		films.put(film.getId(), film);
		log.info("Добавлен фильм: {}", film.getName());
		return film;
	}

	@PutMapping
	public Film updateFilm(@RequestBody final Film film) {
		FilmValidator.validate(film);
		if (film.getId() <= 0) {
			throw new ValidationException("ID фильма должен быть положительным числом");
		}
		if (!films.containsKey(film.getId())) {
			throw new ValidationException("Фильм с ID " + film.getId() + " не найден");
		}
		films.put(film.getId(), film);
		log.info("Обновлён фильм: {}", film.getName());
		return film;
	}

	@GetMapping
	public List<Film> getAllFilms() {
		log.info("Получен список всех фильмов, количество: {}", films.size());
		return new ArrayList<>(films.values());
	}
}
