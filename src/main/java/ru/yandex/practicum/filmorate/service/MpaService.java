package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Slf4j
@Service
public class MpaService {
	private final MpaStorage mpaStorage;

	public MpaService(final MpaStorage mpaStorage) {
		this.mpaStorage = mpaStorage;
	}

	public Mpa getById(final int id) {
		return mpaStorage.getById(id);
	}

	public List<Mpa> getAll() {
		return mpaStorage.getAll();
	}
}
