package handler;

import db.Database;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.HttpRequest;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserHandler 회원가입")
class UserHandlerTest {

    private final UserHandler userHandler = new UserHandler();

    @BeforeEach
    void clearDatabase() throws Exception {
        Field field = Database.class.getDeclaredField("users");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    private HttpRequest requestWithQuery(String queryString) throws Exception {
        String raw = "GET /user/create?" + queryString + " HTTP/1.1\nHost: localhost\n\n";
        return HttpRequest.from(new BufferedReader(new StringReader(raw)));
    }

    @Test
    @DisplayName("회원가입 성공 시 루트로 리다이렉트한다")
    void redirectToRootOnSuccess() throws Exception {
        HttpRequest request = requestWithQuery("userId=gabi&password=1234&name=홍길동");

        String result = userHandler.register(request);

        assertThat(result).isEqualTo("redirect:/");
    }

    @Test
    @DisplayName("회원가입 시 유저가 데이터베이스에 저장된다")
    void saveUserToDatabase() throws Exception {
        HttpRequest request = requestWithQuery("userId=gabi&password=1234&name=홍길동");

        userHandler.register(request);

        assertThat(Database.findUserById("gabi")).isNotNull();
    }

    @Test
    @DisplayName("회원가입 시 입력한 정보로 유저가 저장된다")
    void saveUserWithCorrectInfo() throws Exception {
        HttpRequest request = requestWithQuery("userId=gabi&password=1234&name=홍길동");

        userHandler.register(request);

        User saved = Database.findUserById("gabi");
        assertThat(saved.getUserId()).isEqualTo("gabi");
        assertThat(saved.getPassword()).isEqualTo("1234");
        assertThat(saved.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("이메일 없이 회원가입하면 이메일은 null이다")
    void emailIsNullWhenNotProvided() throws Exception {
        HttpRequest request = requestWithQuery("userId=gabi&password=1234&name=홍길동");

        userHandler.register(request);

        assertThat(Database.findUserById("gabi").getEmail()).isNull();
    }

    @Test
    @DisplayName("URL 인코딩된 이름으로 회원가입할 수 있다")
    void registerWithUrlEncodedName() throws Exception {
        // "홍길동" URL 인코딩: %ED%99%8D%EA%B8%B8%EB%8F%99
        HttpRequest request = requestWithQuery("userId=gabi&password=1234&name=%ED%99%8D%EA%B8%B8%EB%8F%99");

        userHandler.register(request);

        assertThat(Database.findUserById("gabi").getName()).isEqualTo("홍길동");
    }
}
