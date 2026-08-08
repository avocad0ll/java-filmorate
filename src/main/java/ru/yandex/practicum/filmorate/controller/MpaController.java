package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/mpa")
public class MpaController {
	private final MpaService mpaService;

	public MpaController(final MpaService mpaService) {
		this.mpaService = mpaService;
	}

	@GetMapping
	public List<Mpa> getAllMpa() {
		return mpaService.getAll();
	}

	@GetMapping("/{id}")
	public Mpa getMpaById(@PathVariable @Positive final int id) {
		return mpaService.getById(id);
	}
}
