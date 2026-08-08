package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Film {
	private int id;
	private String name;
	private String description;
	private LocalDate releaseDate;
	private int duration;
	private List<Genre> genres = new ArrayList<>();
	private Mpa mpa;
}
