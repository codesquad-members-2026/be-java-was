package jhttp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestParserTest {

    @Test
    @DisplayName("Parses a standard raw HTTP GET request stream")
    void parseGetRequest() throws IOException {
        // given
        String rawRequest = "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Connection: keep-alive\r\n\r\n";
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());

        // when
        HttpRequest request = HttpRequestParser.parse(is);

        // then
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getUrl()).isEqualTo("/index.html");
        assertThat(request.getHeader("Host")).isEqualTo("localhost:8080");
        assertThat(request.getBody()).isNull();
    }

    @Test
    @DisplayName("Parses a POST request including Body and Content-Length")
    void parsePostRequestWithBody() throws IOException {
        // given
        String body = "userID=admin&password=123";
        String rawRequest = "POST /login HTTP/1.1\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: " + body.length() + "\r\n\r\n" +
                body;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());

        // when
        HttpRequest request = HttpRequestParser.parse(is);

        // then
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getBody()).isEqualTo(body.getBytes());
        assertThat(request.getBodyParam("userID")).isEqualTo("admin"); // Ensures setBodyParams() was called
    }
}
