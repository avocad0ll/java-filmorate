package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
	User add(User user);

	User update(User user);

	void delete(int id);

	User getById(int id);

	List<User> getAll();
}
