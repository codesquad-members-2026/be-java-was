package jhttp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestTest {

    @Test
    @DisplayName("Parses Request Line and Query Parameters correctly")
    void parseRequestLineAndParams() {
        // given
        List<String> headers = Arrays.asList(
                "GET /search?query=hello&sort=desc HTTP/1.1",
                "Host: localhost:8080"
        );

        // when
        HttpRequest request = new HttpRequest(headers);

        // then
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getUrl()).isEqualTo("/search");
        assertThat(request.getParam("query")).isEqualTo("hello");
        assertThat(request.getParam("sort")).isEqualTo("desc");
        assertThat(request.getHeader("Host")).isEqualTo("localhost:8080");
    }

    @Test
    @DisplayName("Extracts Session ID from Cookie Header")
    void getSessionID() {
        // given
        List<String> headers = Arrays.asList(
                "GET / HTTP/1.1",
                "Cookie: Idea-2f3b=xyz; SID=my-test-session-id; Other=val"
        );
        HttpRequest request = new HttpRequest(headers);

        // when
        String sessionId = request.getSessionID();

        // then
        assertThat(sessionId).isEqualTo("my-test-session-id");
    }

    @Test
    @DisplayName("Parses URL-encoded Body Parameters")
    void parseBodyParams() {
        // given
        List<String> headers = Arrays.asList("POST /login HTTP/1.1");
        HttpRequest request = new HttpRequest(headers);
        String bodyString = "userID=testUser&password=myPassword%21";
        request.setBody(bodyString.getBytes());

        // when
        request.setBodyParams();

        // then
        assertThat(request.getBodyParam("userID")).isEqualTo("testUser");
        assertThat(request.getBodyParam("password")).isEqualTo("myPassword!"); // Checks URL decoding
    }
}