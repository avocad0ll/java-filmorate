package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
public class UserDbStorage implements UserStorage {
	private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setEmail(rs.getString("email"));
		user.setLogin(rs.getString("login"));
		user.setName(rs.getString("name"));
		user.setBirthday(rs.getObject("birthday", LocalDate.class));
		return user;
	};

	private final JdbcTemplate jdbcTemplate;

	public UserDbStorage(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public User add(final User user) {
		UserValidator.validate(user);
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, user.getEmail());
			ps.setString(2, user.getLogin());
			ps.setString(3, user.getName());
			ps.setObject(4, user.getBirthday());
			return ps;
		}, keyHolder);
		user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
		log.info("Создан пользователь с ID {}: {}", user.getId(), user.getLogin());
		return user;
	}

	@Override
	public User update(final User user) {
		UserValidator.validate(user);
		if (user.getId() <= 0) {
			throw new NotFoundException("ID пользователя должен быть положительным числом");
		}
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
		int rows = jdbcTemplate.update(
				"UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
				user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
		if (rows == 0) {
			throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
		}
		log.info("Обновлён пользователь с ID {}", user.getId());
		return user;
	}

	@Override
	public void delete(final int id) {
		int rows = jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
		if (rows == 0) {
			throw new NotFoundException("Пользователь с ID " + id + " не найден");
		}
		log.info("Удалён пользователь с ID {}", id);
	}

	@Override
	public User getById(final int id) {
		List<User> users = jdbcTemplate.query(
				"SELECT id, email, login, name, birthday FROM users WHERE id = ?", USER_MAPPER, id);
		if (users.isEmpty()) {
			throw new NotFoundException("Пользователь с ID " + id + " не найден");
		}
		return users.get(0);
	}

	@Override
	public List<User> getAll() {
		return jdbcTemplate.query("SELECT id, email, login, name, birthday FROM users ORDER BY id", USER_MAPPER);
	}

	@Override
	public void addFriend(final int userId, final int friendId) {
		String reverseStatus = jdbcTemplate.query(
				"SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?",
				rs -> rs.next() ? rs.getString("status") : null, friendId, userId);
		if (reverseStatus != null) {
			jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)",
					userId, friendId, FriendshipStatus.CONFIRMED.name());
			jdbcTemplate.update("UPDATE friendship SET status = ? WHERE user_id = ? AND friend_id = ?",
					FriendshipStatus.CONFIRMED.name(), friendId, userId);
			log.info("Дружба между {} и {} подтверждена", userId, friendId);
		} else {
			jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)",
					userId, friendId, FriendshipStatus.UNCONFIRMED.name());
			log.info("Пользователь {} отправил запрос в друзья {}", userId, friendId);
		}
	}

	@Override
	public void removeFriend(final int userId, final int friendId) {
		jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ? AND friend_id = ?", userId, friendId);
		log.info("Пользователь {} удалил из друзей {}", userId, friendId);
	}

	@Override
	public List<User> getUserFriends(final int userId) {
		return jdbcTemplate.query(
				"SELECT u.id, u.email, u.login, u.name, u.birthday "
						+ "FROM friendship f JOIN users u ON u.id = f.friend_id "
						+ "WHERE f.user_id = ? ORDER BY u.id",
				USER_MAPPER, userId);
	}

	@Override
	public List<User> getCommonFriends(final int userId, final int otherId) {
		return jdbcTemplate.query(
				"SELECT u.id, u.email, u.login, u.name, u.birthday "
						+ "FROM friendship f1 "
						+ "JOIN friendship f2 ON f1.friend_id = f2.friend_id "
						+ "JOIN users u ON u.id = f1.friend_id "
						+ "WHERE f1.user_id = ? AND f2.user_id = ? ORDER BY u.id",
				USER_MAPPER, userId, otherId);
	}
}
