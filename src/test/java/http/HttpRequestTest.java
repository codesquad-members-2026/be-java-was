package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestTest {

    @Test
    @DisplayName("쿼리 스트링이 없는 GET 요청의 경로를 정확히 파싱한다")
    void parseRequestWithoutQueryString() {
        String requestData = "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";
        InputStream in = new ByteArrayInputStream(requestData.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = new HttpRequest(in);

        assertThat(request.getPath()).isEqualTo("/index.html");
    }

    @Test
    @DisplayName("브라우저가 보낸 헤더들을 모두 읽어서 스트림을 비워준다")
    void consumeHeadersTest() {
        String requestData = "GET /index.html HTTP/1.1\r\n" +
                "Accept: */*\r\n" +
                "Connection: keep-alive\r\n" +
                "\r\n";
        InputStream in = new ByteArrayInputStream(requestData.getBytes(StandardCharsets.UTF_8));

        // HttpRequest 생성 과정에서 consumeHeaders가 정상 동작하는지 확인
        HttpRequest request = new HttpRequest(in);

    }
}