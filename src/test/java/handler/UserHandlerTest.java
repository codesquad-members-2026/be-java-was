package handler;

import db.Database;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import webserver.http.HttpRequest;
import webserver.http.HttpRequestParser;
import webserver.session.Session;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserHandler")
class UserHandlerTest {

    private final UserHandler userHandler = new UserHandler();
    private final HttpRequestParser parser = new HttpRequestParser();

    @BeforeEach
    void clearDatabase() throws Exception {
        Field field = Database.class.getDeclaredField("users");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    private HttpRequest postWithBody(String path, String body) throws Exception {
        String raw = "POST " + path + " HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "\r\n" + body;
        return parser.parse(new BufferedInputStream(
                new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8))));
    }

    @Nested
    @DisplayName("회원가입")
    class Register {

        @Test
        @DisplayName("회원가입 성공 시 루트로 리다이렉트한다")
        void redirectToRootOnSuccess() throws Exception {
            HttpRequest request = postWithBody("/user/create",
                    "userId=gabi&password=1234&name=홍길동");

            String result = userHandler.register(request);

            assertThat(result).isEqualTo("redirect:/");
        }

        @Test
        @DisplayName("회원가입 시 유저가 데이터베이스에 저장된다")
        void saveUserToDatabase() throws Exception {
            HttpRequest request = postWithBody("/user/create",
                    "userId=gabi&password=1234&name=홍길동");

            userHandler.register(request);

            assertThat(Database.findUserById("gabi")).isNotNull();
        }

        @Test
        @DisplayName("회원가입 시 입력한 정보로 유저가 저장된다")
        void saveUserWithCorrectInfo() throws Exception {
            HttpRequest request = postWithBody("/user/create",
                    "userId=gabi&password=1234&name=홍길동");

            userHandler.register(request);

            User saved = Database.findUserById("gabi");
            assertThat(saved.getUserId()).isEqualTo("gabi");
            assertThat(saved.getPassword()).isEqualTo("1234");
            assertThat(saved.getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("이메일 없이 회원가입하면 이메일은 null이다")
        void emailIsNullWhenNotProvided() throws Exception {
            HttpRequest request = postWithBody("/user/create",
                    "userId=gabi&password=1234&name=홍길동");

            userHandler.register(request);

            assertThat(Database.findUserById("gabi").getEmail()).isNull();
        }

        @Test
        @DisplayName("URL 인코딩된 이름으로 회원가입할 수 있다")
        void registerWithUrlEncodedName() throws Exception {
            HttpRequest request = postWithBody("/user/create",
                    "userId=gabi&password=1234&name=%ED%99%8D%EA%B8%B8%EB%8F%99");

            userHandler.register(request);

            assertThat(Database.findUserById("gabi").getName()).isEqualTo("홍길동");
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("성공 시 루트로 리다이렉트한다")
        void redirectToRootOnSuccess() throws Exception {
            Database.addUser(new User("gabi", "1234", "홍길동", null));
            HttpRequest request = postWithBody("/user/login", "userId=gabi&password=1234");
            Session session = new Session("test-session");

            String result = userHandler.login(request, session);

            assertThat(result).isEqualTo("redirect:/");
        }

        @Test
        @DisplayName("성공 시 세션에 user 속성이 저장된다")
        void saveUserInSessionOnSuccess() throws Exception {
            User user = new User("gabi", "1234", "홍길동", null);
            Database.addUser(user);
            HttpRequest request = postWithBody("/user/login", "userId=gabi&password=1234");
            Session session = new Session("test-session");

            userHandler.login(request, session);

            assertThat(session.get("user")).isEqualTo(user);
        }

        @Test
        @DisplayName("존재하지 않는 유저는 실패 페이지를 반환한다")
        void returnFailPageWhenUserNotFound() throws Exception {
            HttpRequest request = postWithBody("/user/login", "userId=unknown&password=1234");
            Session session = new Session("test-session");

            String result = userHandler.login(request, session);

            assertThat(result).isEqualTo("/login/login_failed.html");
            assertThat(session.get("user")).isNull();
        }

        @Test
        @DisplayName("비밀번호가 틀리면 실패 페이지를 반환한다")
        void returnFailPageWhenPasswordMismatch() throws Exception {
            Database.addUser(new User("gabi", "1234", "홍길동", null));
            HttpRequest request = postWithBody("/user/login", "userId=gabi&password=wrong");
            Session session = new Session("test-session");

            String result = userHandler.login(request, session);

            assertThat(result).isEqualTo("/login/login_failed.html");
            assertThat(session.get("user")).isNull();
        }
    }
}
