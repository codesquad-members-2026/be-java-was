package webserver.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.session.Cookie;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HttpResponse 출력")
class HttpResponseTest {

    private ByteArrayOutputStream baos;
    private HttpResponse response;

    private HttpResponse newResponse() {
        baos = new ByteArrayOutputStream();
        return new HttpResponse(new DataOutputStream(baos));
    }

    private String flushed() {
        return baos.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("기본 상태는 200 OK이다")
    void defaultStatusIsOk() throws Exception {
        response = newResponse();
        response.flush();

        assertThat(flushed()).startsWith("HTTP/1.1 200 OK\r\n");
    }

    @Test
    @DisplayName("302 리다이렉트 시 Location 헤더를 쓴다")
    void writeLocationOnRedirect() throws Exception {
        response = newResponse();
        response.sendRedirect("/");
        response.flush();

        String output = flushed();
        assertThat(output).startsWith("HTTP/1.1 302 Found\r\n");
        assertThat(output).contains("Location: /\r\n");
    }

    @Test
    @DisplayName("Content-Length는 body 바이트 수와 일치한다")
    void contentLengthMatchesBody() throws Exception {
        response = newResponse();
        response.write("hello");
        response.flush();

        assertThat(flushed()).contains("Content-Length: 5\r\n");
    }

    @Test
    @DisplayName("쿠키를 추가하면 Set-Cookie 헤더로 내려간다")
    void writeCookieAsSetCookieHeader() throws Exception {
        response = newResponse();
        response.addCookie(new Cookie("sessionId", "abc123", "/", 7200));
        response.flush();

        assertThat(flushed()).contains("Set-Cookie: sessionId=abc123; Path=/; Max-Age=7200\r\n");
    }

    @Test
    @DisplayName("여러 쿠키는 각각 별개의 Set-Cookie 헤더로 내려간다")
    void writeMultipleCookies() throws Exception {
        response = newResponse();
        response.addCookie(new Cookie("sessionId", "abc", "/", null));
        response.addCookie(new Cookie("theme", "dark", null, null));
        response.flush();

        String output = flushed();
        assertThat(output).contains("Set-Cookie: sessionId=abc; Path=/\r\n");
        assertThat(output).contains("Set-Cookie: theme=dark\r\n");
    }

    @Test
    @DisplayName("쿠키가 없으면 Set-Cookie 헤더가 없다")
    void noSetCookieHeaderWhenNoCookies() throws Exception {
        response = newResponse();
        response.flush();

        assertThat(flushed()).doesNotContain("Set-Cookie");
    }

    @Test
    @DisplayName("헤더와 body는 빈 줄로 구분된다")
    void blankLineBetweenHeadersAndBody() throws Exception {
        response = newResponse();
        response.write("body");
        response.flush();

        assertThat(flushed()).contains("\r\n\r\nbody");
    }

    @Test
    @DisplayName("reset 이후 body는 비워진다")
    void resetClearsBody() throws Exception {
        response = newResponse();
        response.write("should be cleared");
        response.reset();
        response.write("ok");
        response.flush();

        String output = flushed();
        assertThat(output).contains("Content-Length: 2\r\n");
        assertThat(output).endsWith("\r\n\r\nok");
    }
}
