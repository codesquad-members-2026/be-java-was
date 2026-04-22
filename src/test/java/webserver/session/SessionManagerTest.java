package webserver.session;

import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http.HttpRequest;
import webserver.http.HttpRequestParser;
import webserver.http.HttpResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SessionManager 쿠키-세션 흐름")
class SessionManagerTest {

    private final HttpRequestParser parser = new HttpRequestParser();
    private final SessionManager sessionManager = new SessionManager();

    private HttpRequest parse(String raw) throws Exception {
        return parser.parse(new BufferedInputStream(
                new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8))));
    }

    private HttpResponse newResponse(ByteArrayOutputStream baos) {
        return new HttpResponse(new DataOutputStream(baos));
    }

    @SuppressWarnings("unchecked")
    private List<Cookie> cookiesOf(HttpResponse response) throws Exception {
        Field f = HttpResponse.class.getDeclaredField("cookies");
        f.setAccessible(true);
        return (List<Cookie>) f.get(response);
    }

    @Test
    @DisplayName("Cookie 헤더가 없으면 새 세션을 만들고 응답에 sessionId 쿠키를 추가한다")
    void createNewSessionWhenNoCookie() throws Exception {
        HttpRequest request = parse("GET / HTTP/1.1\r\nHost: localhost\r\n\r\n");
        HttpResponse response = newResponse(new ByteArrayOutputStream());

        Session session = sessionManager.get(request, response);

        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotBlank();

        List<Cookie> cookies = cookiesOf(response);
        assertThat(cookies).hasSize(1);
        Cookie cookie = cookies.get(0);
        assertThat(cookie.name()).isEqualTo(SessionManager.SESSION_COOKIE_NAME);
        assertThat(cookie.value()).isEqualTo(session.getId());
        assertThat(cookie.path()).isEqualTo("/");
    }

    @Test
    @DisplayName("유효한 sessionId 쿠키가 오면 기존 세션을 반환하고 새 쿠키를 추가하지 않는다")
    void reuseExistingSession() throws Exception {
        // 1. 첫 요청 — 새 세션 생성
        HttpRequest first = parse("GET / HTTP/1.1\r\nHost: localhost\r\n\r\n");
        HttpResponse firstResponse = newResponse(new ByteArrayOutputStream());
        Session created = sessionManager.get(first, firstResponse);
        String sessionId = created.getId();

        // 2. 같은 sessionId를 Cookie 헤더로 다시 보냄
        HttpRequest second = parse("GET / HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Cookie: " + SessionManager.SESSION_COOKIE_NAME + "=" + sessionId + "\r\n\r\n");
        HttpResponse secondResponse = newResponse(new ByteArrayOutputStream());

        Session reused = sessionManager.get(second, secondResponse);

        assertThat(reused).isSameAs(created);
        assertThat(cookiesOf(secondResponse)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 sessionId가 오면 새 세션을 만든다")
    void createNewSessionWhenUnknownCookie() throws Exception {
        HttpRequest request = parse("GET / HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Cookie: " + SessionManager.SESSION_COOKIE_NAME + "=does-not-exist\r\n\r\n");
        HttpResponse response = newResponse(new ByteArrayOutputStream());

        Session session = sessionManager.get(request, response);

        assertThat(session.getId()).isNotEqualTo("does-not-exist");
        assertThat(cookiesOf(response)).hasSize(1);
    }

    @Test
    @DisplayName("여러 쿠키 중 sessionId만 골라서 세션을 찾는다")
    void pickSessionIdAmongMultipleCookies() throws Exception {
        // 먼저 세션을 하나 만들어 두고
        HttpRequest first = parse("GET / HTTP/1.1\r\nHost: localhost\r\n\r\n");
        Session created = sessionManager.get(first, newResponse(new ByteArrayOutputStream()));

        // 다른 쿠키들과 섞어 보내도 sessionId만 골라 쓰는지
        HttpRequest second = parse("GET / HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Cookie: theme=dark; " + SessionManager.SESSION_COOKIE_NAME + "=" + created.getId() + "; lang=ko\r\n\r\n");

        Session reused = sessionManager.get(second, newResponse(new ByteArrayOutputStream()));

        assertThat(reused).isSameAs(created);
    }

    @Test
    @DisplayName("쿠키 라운드트립: 로그인 시 담은 값을 다음 요청에서 꺼낼 수 있다")
    void cookieRoundTripPreservesSessionAttribute() throws Exception {
        User user = new User("gabi", "1234", "홍길동", null);

        // 1. 로그인 요청 — 쿠키 없음
        HttpRequest loginRequest = parse("POST /user/login HTTP/1.1\r\nHost: localhost\r\n\r\n");
        HttpResponse loginResponse = newResponse(new ByteArrayOutputStream());

        Session loginSession = sessionManager.get(loginRequest, loginResponse);
        loginSession.addAttribute("user", user);

        // 2. 응답에 담긴 sessionId 쿠키를 추출
        List<Cookie> issued = cookiesOf(loginResponse);
        assertThat(issued).hasSize(1);
        Cookie sessionCookie = issued.get(0);
        String sessionId = sessionCookie.value();

        // 3. 브라우저가 Cookie 헤더로 돌려보낸다고 가정한 후속 요청
        HttpRequest nextRequest = parse("GET /test HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Cookie: " + sessionCookie.name() + "=" + sessionId + "\r\n\r\n");
        HttpResponse nextResponse = newResponse(new ByteArrayOutputStream());

        Session nextSession = sessionManager.get(nextRequest, nextResponse);

        // 4. 같은 세션이어야 하고, 로그인 시 넣어둔 값이 그대로 나와야 함
        assertThat(nextSession).isSameAs(loginSession);
        assertThat(nextSession.get("user")).isEqualTo(user);
    }

    @Test
    @DisplayName("쿠키 라운드트립: 실제 HttpResponse 출력 바이트에서 sessionId를 파싱해 재조회한다")
    void cookieRoundTripThroughRawBytes() throws Exception {
        // 1. 세션 생성 + 응답 flush
        HttpRequest loginRequest = parse("POST /user/login HTTP/1.1\r\nHost: localhost\r\n\r\n");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpResponse loginResponse = newResponse(baos);
        Session loginSession = sessionManager.get(loginRequest, loginResponse);
        loginSession.addAttribute("userId", "gabi");
        loginResponse.flush();

        // 2. 실제 바이트에서 Set-Cookie 헤더를 파싱
        String raw = baos.toString(StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("Set-Cookie: " + SessionManager.SESSION_COOKIE_NAME + "=([^;\\r\\n]+)").matcher(raw);
        assertThat(m.find())
                .as("응답에 Set-Cookie 헤더가 있어야 함. 실제 응답:\n" + raw)
                .isTrue();
        String sessionIdFromWire = m.group(1);

        // 3. 그 값을 Cookie 헤더에 실어 후속 요청
        HttpRequest nextRequest = parse("GET / HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Cookie: " + SessionManager.SESSION_COOKIE_NAME + "=" + sessionIdFromWire + "\r\n\r\n");
        Session nextSession = sessionManager.get(nextRequest, newResponse(new ByteArrayOutputStream()));

        // 4. 저장한 속성이 그대로 꺼내진다
        assertThat(nextSession.get("userId")).isEqualTo("gabi");
    }
}
