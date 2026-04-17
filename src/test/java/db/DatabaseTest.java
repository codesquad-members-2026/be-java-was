package db;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Database CRUD")
class DatabaseTest {

    @BeforeEach
    void clearDatabase() throws Exception {
        Field field = Database.class.getDeclaredField("users");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    @Test
    @DisplayName("유저를 저장하고 아이디로 조회할 수 있다")
    void saveAndFindById() {
        User user = new User("gabi", "1234", "가비", "gabi@test.com");

        Database.addUser(user);

        assertThat(Database.findUserById("gabi")).isEqualTo(user);
    }

    @Test
    @DisplayName("존재하지 않는 아이디 조회 시 null을 반환한다")
    void returnNullWhenNotFound() {
        assertThat(Database.findUserById("nobody")).isNull();
    }

    @Test
    @DisplayName("전체 유저를 조회할 수 있다")
    void findAll() {
        User user1 = new User("user1", "pw1", "유저1", null);
        User user2 = new User("user2", "pw2", "유저2", null);
        Database.addUser(user1);
        Database.addUser(user2);

        assertThat(Database.findAll()).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    @DisplayName("유저가 없으면 빈 컬렉션을 반환한다")
    void findAllReturnsEmptyWhenNoUsers() {
        assertThat(Database.findAll()).isEmpty();
    }

    @Test
    @DisplayName("같은 아이디로 저장하면 덮어쓴다")
    void overwriteOnDuplicateId() {
        User original = new User("gabi", "1234", "가비", null);
        User updated = new User("gabi", "5678", "가비수정", null);

        Database.addUser(original);
        Database.addUser(updated);

        assertThat(Database.findUserById("gabi")).isEqualTo(updated);
        assertThat(Database.findAll()).hasSize(1);
    }
}
