package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
	private final UserService userService;

	public UserController(final UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public User createUser(@RequestBody final User user) {
		return userService.add(user);
	}

	@PutMapping
	public User updateUser(@RequestBody final User user) {
		return userService.update(user);
	}

	@GetMapping
	public List<User> getAllUsers() {
		return userService.getAll();
	}

	@GetMapping("/{id}")
	public User getUserById(@PathVariable final int id) {
		return userService.getById(id);
	}

	@PutMapping("/{id}/friends/{friendId}")
	public void addFriend(@PathVariable final int id, @PathVariable final int friendId) {
		userService.addFriend(id, friendId);
	}

	@DeleteMapping("/{id}/friends/{friendId}")
	public void removeFriend(@PathVariable final int id, @PathVariable final int friendId) {
		userService.removeFriend(id, friendId);
	}

	@GetMapping("/{id}/friends")
	public List<User> getFriends(@PathVariable final int id) {
		return userService.getFriends(id);
	}

	@GetMapping("/{id}/friends/common/{otherId}")
	public List<User> getCommonFriends(@PathVariable final int id, @PathVariable final int otherId) {
		return userService.getCommonFriends(id, otherId);
	}
}
