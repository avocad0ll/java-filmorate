package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
	private final FilmService filmService;

	public FilmController(final FilmService filmService) {
		this.filmService = filmService;
	}

	@PostMapping
	public Film addFilm(@RequestBody final Film film) {
		return filmService.add(film);
	}

	@PutMapping
	public Film updateFilm(@RequestBody final Film film) {
		return filmService.update(film);
	}

	@GetMapping
	public List<Film> getAllFilms() {
		return filmService.getAll();
	}

	@GetMapping("/{id}")
	public Film getFilmById(@PathVariable final int id) {
		return filmService.getById(id);
	}

	@PutMapping("/{id}/like/{userId}")
	public void addLike(@PathVariable final int id, @PathVariable final int userId) {
		filmService.addLike(id, userId);
	}

	@DeleteMapping("/{id}/like/{userId}")
	public void removeLike(@PathVariable final int id, @PathVariable final int userId) {
		filmService.removeLike(id, userId);
	}

	@GetMapping("/popular")
	public List<Film> getPopular(@RequestParam(defaultValue = "10") final int count) {
		log.info("Запрос популярных фильмов, количество: {}", count);
		return filmService.getPopular(count);
	}
}
