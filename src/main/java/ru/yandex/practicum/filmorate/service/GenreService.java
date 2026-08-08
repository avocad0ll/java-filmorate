package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@Slf4j
@Service
public class GenreService {
	private final GenreStorage genreStorage;

	public GenreService(final GenreStorage genreStorage) {
		this.genreStorage = genreStorage;
	}

	public Genre getById(final int id) {
		return genreStorage.getById(id);
	}

	public List<Genre> getAll() {
		return genreStorage.getAll();
	}
}
