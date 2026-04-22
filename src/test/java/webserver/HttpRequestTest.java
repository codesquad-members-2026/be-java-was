package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http.HttpRequest;
import webserver.http.HttpRequestParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HttpRequest 파싱")
class HttpRequestTest {

    private final HttpRequestParser parser = new HttpRequestParser();

    private HttpRequest parse(String raw) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        return parser.parse(new BufferedInputStream(bais));
    }

    @Test
    @DisplayName("요청 라인에서 메서드, 경로, 프로토콜을 파싱한다")
    void parseRequestLine() throws Exception {
        HttpRequest req = parse("GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/index.html");
        assertThat(req.getProtocol()).isEqualTo("HTTP/1.1");
    }

    @Test
    @DisplayName("쿼리 파라미터가 없으면 경로만 파싱한다")
    void parsePathWithoutQuery() throws Exception {
        HttpRequest req = parse("GET /user/create HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertThat(req.getPath()).isEqualTo("/user/create");
        assertThat(req.getQueryParameter("any")).isNull();
    }

    @Test
    @DisplayName("쿼리 파라미터 여러 개를 파싱한다")
    void parseQueryParameters() throws Exception {
        HttpRequest req = parse("GET /user/create?userId=gabi&password=1234&name=test HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertThat(req.getQueryParameter("userId")).isEqualTo("gabi");
        assertThat(req.getQueryParameter("password")).isEqualTo("1234");
        assertThat(req.getQueryParameter("name")).isEqualTo("test");
    }

    @Test
    @DisplayName("URL 인코딩된 쿼리 파라미터를 디코딩한다")
    void decodeUrlEncodedQueryParameter() throws Exception {
        // "홍길동" URL 인코딩: %ED%99%8D%EA%B8%B8%EB%8F%99
        HttpRequest req = parse("GET /user/create?name=%ED%99%8D%EA%B8%B8%EB%8F%99&userId=gabi HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertThat(req.getQueryParameter("name")).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("값이 없는 쿼리 파라미터는 빈 문자열로 처리한다")
    void parseEmptyValueQueryParameter() throws Exception {
        HttpRequest req = parse("GET /user/create?key= HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertThat(req.getQueryParameter("key")).isEqualTo("");
    }

    @Test
    @DisplayName("헤더를 파싱한다")
    void parseHeaders() throws Exception {
        HttpRequest req = parse("GET /index.html HTTP/1.1\r\nContent-Type: text/html\r\nAccept: application/json\r\n\r\n");

        assertThat(req.getHeader("Content-Type")).isEqualTo("text/html");
        assertThat(req.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    @DisplayName("Cookie 헤더를 그대로 읽을 수 있다")
    void parseCookieHeader() throws Exception {
        HttpRequest req = parse("GET / HTTP/1.1\r\nHost: localhost\r\nCookie: sessionId=abc123; theme=dark\r\n\r\n");

        assertThat(req.getHeader("Cookie")).isEqualTo("sessionId=abc123; theme=dark");
    }

    @Test
    @DisplayName("POST 요청의 form body를 파싱한다")
    void parsePostBody() throws Exception {
        String body = "userId=gabi&password=1234&name=test";
        String raw = "POST /user/create HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "\r\n" + body;

        HttpRequest req = parse(raw);

        assertThat(req.getBody("userId")).isEqualTo("gabi");
        assertThat(req.getBody("password")).isEqualTo("1234");
        assertThat(req.getBody("name")).isEqualTo("test");
    }

    @Test
    @DisplayName("POST body의 URL 인코딩된 값을 디코딩한다")
    void decodeUrlEncodedPostBody() throws Exception {
        String body = "userId=gabi&name=%ED%99%8D%EA%B8%B8%EB%8F%99";
        String raw = "POST /user/create HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "\r\n" + body;

        HttpRequest req = parse(raw);

        assertThat(req.getBody("name")).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("요청 라인이 없으면 예외가 발생한다")
    void throwExceptionOnEmptyRequest() {
        assertThatThrownBy(() -> parse(""))
                .isInstanceOf(Exception.class);
    }
}
