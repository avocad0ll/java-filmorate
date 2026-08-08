package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Repository
public class MpaDbStorage implements MpaStorage {
	private static final RowMapper<Mpa> MPA_MAPPER = (rs, rowNum) -> {
		Mpa mpa = new Mpa();
		mpa.setId(rs.getInt("id"));
		mpa.setName(rs.getString("name"));
		return mpa;
	};

	private final JdbcTemplate jdbcTemplate;

	public MpaDbStorage(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Mpa getById(final int id) {
		List<Mpa> mpaList = jdbcTemplate.query("SELECT id, name FROM mpa_ratings WHERE id = ?", MPA_MAPPER, id);
		if (mpaList.isEmpty()) {
			throw new NotFoundException("Рейтинг с ID " + id + " не найден");
		}
		return mpaList.get(0);
	}

	@Override
	public List<Mpa> getAll() {
		return jdbcTemplate.query("SELECT id, name FROM mpa_ratings ORDER BY id", MPA_MAPPER);
	}
}
