package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@Repository
public class GenreDbStorage implements GenreStorage {
	private static final RowMapper<Genre> GENRE_MAPPER = (rs, rowNum) -> {
		Genre genre = new Genre();
		genre.setId(rs.getInt("id"));
		genre.setName(rs.getString("name"));
		return genre;
	};

	private final JdbcTemplate jdbcTemplate;

	public GenreDbStorage(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Genre getById(final int id) {
		List<Genre> genres = jdbcTemplate.query("SELECT id, name FROM genres WHERE id = ?", GENRE_MAPPER, id);
		if (genres.isEmpty()) {
			throw new NotFoundException("Жанр с ID " + id + " не найден");
		}
		return genres.get(0);
	}

	@Override
	public List<Genre> getAll() {
		return jdbcTemplate.query("SELECT id, name FROM genres ORDER BY id", GENRE_MAPPER);
	}
}
