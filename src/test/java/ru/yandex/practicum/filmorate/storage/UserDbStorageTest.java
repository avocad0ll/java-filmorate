package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
	private final UserDbStorage userStorage;
	private final JdbcTemplate jdbcTemplate;

	private User createUser(final String email, final String login, final String name) {
		User user = new User();
		user.setEmail(email);
		user.setLogin(login);
		user.setName(name);
		user.setBirthday(LocalDate.of(1990, 1, 1));
		return userStorage.add(user);
	}

	@Test
	void addReturnsUserWithGeneratedId() {
		User user = createUser("alice@mail.ru", "alice", "Alice");

		assertThat(user.getId()).isPositive();
		User saved = userStorage.getById(user.getId());
		assertThat(saved.getEmail()).isEqualTo("alice@mail.ru");
		assertThat(saved.getLogin()).isEqualTo("alice");
		assertThat(saved.getName()).isEqualTo("Alice");
		assertThat(saved.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
	}

	@Test
	void addUsesLoginAsNameWhenNameIsBlank() {
		User user = createUser("bob@mail.ru", "bob", " ");

		assertThat(userStorage.getById(user.getId()).getName()).isEqualTo("bob");
	}

	@Test
	void getByIdThrowsWhenUserNotFound() {
		assertThatThrownBy(() -> userStorage.getById(999))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void updateChangesUserData() {
		User user = createUser("alice@mail.ru", "alice", "Alice");
		user.setName("AliceUpdated");
		user.setEmail("new@mail.ru");

		User updated = userStorage.update(user);

		assertThat(updated.getName()).isEqualTo("AliceUpdated");
		assertThat(userStorage.getById(user.getId()).getEmail()).isEqualTo("new@mail.ru");
	}

	@Test
	void updateThrowsWhenUserNotFound() {
		User user = createUser("alice@mail.ru", "alice", "Alice");
		user.setId(999);

		assertThatThrownBy(() -> userStorage.update(user))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void getAllReturnsAllUsers() {
		createUser("alice@mail.ru", "alice", "Alice");
		createUser("bob@mail.ru", "bob", "Bob");

		List<User> users = userStorage.getAll();

		assertThat(users).hasSize(2);
		assertThat(users).extracting(User::getLogin).containsExactly("alice", "bob");
	}

	@Test
	void deleteRemovesUser() {
		User user = createUser("alice@mail.ru", "alice", "Alice");

		userStorage.delete(user.getId());

		assertThatThrownBy(() -> userStorage.getById(user.getId()))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void addFriendAddsUserToFriendList() {
		User alice = createUser("alice@mail.ru", "alice", "Alice");
		User bob = createUser("bob@mail.ru", "bob", "Bob");

		userStorage.addFriend(alice.getId(), bob.getId());

		List<User> friends = userStorage.getUserFriends(alice.getId());
		assertThat(friends).extracting(User::getId).containsExactly(bob.getId());
		assertThat(userStorage.getUserFriends(bob.getId())).isEmpty();
	}

	@Test
	void friendshipBecomesConfirmedWhenMutual() {
		User alice = createUser("alice@mail.ru", "alice", "Alice");
		User bob = createUser("bob@mail.ru", "bob", "Bob");

		userStorage.addFriend(alice.getId(), bob.getId());
		String firstStatus = jdbcTemplate.queryForObject(
				"SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?",
				String.class, alice.getId(), bob.getId());
		assertThat(firstStatus).isEqualTo("UNCONFIRMED");

		userStorage.addFriend(bob.getId(), alice.getId());
		String aliceStatus = jdbcTemplate.queryForObject(
				"SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?",
				String.class, alice.getId(), bob.getId());
		String bobStatus = jdbcTemplate.queryForObject(
				"SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?",
				String.class, bob.getId(), alice.getId());
		assertThat(aliceStatus).isEqualTo("CONFIRMED");
		assertThat(bobStatus).isEqualTo("CONFIRMED");
	}

	@Test
	void removeFriendRemovesUserFromFriendList() {
		User alice = createUser("alice@mail.ru", "alice", "Alice");
		User bob = createUser("bob@mail.ru", "bob", "Bob");
		userStorage.addFriend(alice.getId(), bob.getId());

		userStorage.removeFriend(alice.getId(), bob.getId());

		assertThat(userStorage.getUserFriends(alice.getId())).isEmpty();
	}

	@Test
	void getCommonFriendsReturnsSharedFriends() {
		User alice = createUser("alice@mail.ru", "alice", "Alice");
		User bob = createUser("bob@mail.ru", "bob", "Bob");
		User charlie = createUser("charlie@mail.ru", "charlie", "Charlie");
		userStorage.addFriend(alice.getId(), charlie.getId());
		userStorage.addFriend(bob.getId(), charlie.getId());

		List<User> common = userStorage.getCommonFriends(alice.getId(), bob.getId());

		assertThat(common).extracting(User::getId).containsExactly(charlie.getId());
	}
}
