package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HttpRequest 파싱")
class HttpRequestTest {

    private HttpRequest request(String raw) throws Exception {
        return HttpRequest.from(new BufferedReader(new StringReader(raw)));
    }

    @Test
    @DisplayName("요청 라인에서 메서드, 경로, 프로토콜을 파싱한다")
    void parseRequestLine() throws Exception {
        HttpRequest req = request("GET /index.html HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/index.html");
        assertThat(req.getProtocol()).isEqualTo("HTTP/1.1");
    }

    @Test
    @DisplayName("쿼리 파라미터가 없으면 경로를 그대로 파싱한다")
    void parsePathWithoutQuery() throws Exception {
        HttpRequest req = request("GET /user/create HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.getPath()).isEqualTo("/user/create");
        assertThat(req.getQueryParameters()).isEmpty();
    }

    @Test
    @DisplayName("쿼리 파라미터 여러 개를 파싱한다")
    void parseQueryParameters() throws Exception {
        HttpRequest req = request("GET /user/create?userId=gabi&password=1234&name=test HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.getQueryParameters())
                .containsEntry("userId", "gabi")
                .containsEntry("password", "1234")
                .containsEntry("name", "test");
    }

    @Test
    @DisplayName("URL 인코딩된 쿼리 파라미터를 디코딩한다")
    void decodeUrlEncodedQueryParameter() throws Exception {
        // "홍길동" URL 인코딩: %ED%99%8D%EA%B8%B8%EB%8F%99
        HttpRequest req = request("GET /user/create?name=%ED%99%8D%EA%B8%B8%EB%8F%99&userId=gabi HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.getQueryParameters().get("name")).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("값이 없는 쿼리 파라미터는 빈 문자열로 처리한다")
    void parseEmptyValueQueryParameter() throws Exception {
        HttpRequest req = request("GET /user/create?key= HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.getQueryParameters().get("key")).isEqualTo("");
    }

    @Test
    @DisplayName("빈 값 파라미터가 섞여 있어도 파싱한다")
    void parseMixedQueryParameters() throws Exception {
        HttpRequest req = request("GET /user/create?a=1&b=&c=3 HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.getQueryParameters())
                .containsEntry("a", "1")
                .containsEntry("b", "")
                .containsEntry("c", "3");
    }

    @Test
    @DisplayName("html 요청은 정적 리소스이다")
    void htmlIsStaticResource() throws Exception {
        HttpRequest req = request("GET /index.html HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.isStaticResource()).isTrue();
    }

    @Test
    @DisplayName("css 요청은 정적 리소스이다")
    void cssIsStaticResource() throws Exception {
        HttpRequest req = request("GET /main.css HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.isStaticResource()).isTrue();
    }

    @Test
    @DisplayName("ico 요청은 정적 리소스이다")
    void icoIsStaticResource() throws Exception {
        HttpRequest req = request("GET /favicon.ico HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.isStaticResource()).isTrue();
    }

    @Test
    @DisplayName("svg 요청은 정적 리소스이다")
    void svgIsStaticResource() throws Exception {
        HttpRequest req = request("GET /logo.svg HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.isStaticResource()).isTrue();
    }

    @Test
    @DisplayName("동적 경로 요청은 정적 리소스가 아니다")
    void dynamicPathIsNotStaticResource() throws Exception {
        HttpRequest req = request("GET /user/create?userId=gabi HTTP/1.1\nHost: localhost\n\n");

        assertThat(req.isStaticResource()).isFalse();
    }

    @Test
    @DisplayName("헤더를 파싱한다")
    void parseHeaders() throws Exception {
        HttpRequest req = request("GET /index.html HTTP/1.1\nContent-Type: text/html\nAccept: application/json\n\n");

        assertThat(req.getHeaders())
                .containsEntry("Content-Type", "text/html")
                .containsEntry("Accept", "application/json");
    }

    @Test
    @DisplayName("요청 라인이 없으면 예외가 발생한다")
    void throwExceptionOnEmptyRequest() {
        assertThatThrownBy(() -> request(""))
                .isInstanceOf(Exception.class);
    }
}
