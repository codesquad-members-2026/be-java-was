package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.request.HttpRequest;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestTest {

    @Test
    @DisplayName("GET 요청 시 쿼리 스트링이 정상적으로 파싱되고 한글이 디코딩되어야 한다.")
    void parseHttpRequestWithQueryString() throws Exception {
        // given: 회원가입 요청과 유사한 가짜 HTTP 메시지 생성
        String requestMessage = "GET /create?userId=javajigi&password=password&name=%EB%B0%95%EC%9E%AC%EC%84%B1 HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Connection: keep-alive\r\n" +
                "Accept: */*\r\n\r\n"; // 헤더의 끝을 의미하는 개행

        BufferedReader br = new BufferedReader(new StringReader(requestMessage));

        // when: 파싱 로직 실행
        HttpRequest request = HttpRequest.of(br);

        // then: 분리된 Path와 프로토콜 확인
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/create");
        assertThat(request.getProtocol()).isEqualTo("HTTP/1.1");

        // then: 파라미터가 3개 잘 쪼개졌는지, 한글 디코딩은 잘 되었는지 확인
        assertThat(request.getParameters()).hasSize(3);
        assertThat(request.getParameters().get("userId")).isEqualTo("javajigi");
        assertThat(request.getParameters().get("password")).isEqualTo("password");
        assertThat(request.getParameters().get("name")).isEqualTo("박재성"); // 디코딩 완벽 확인!
    }

    @Test
    @DisplayName("쿼리 스트링이 없는 일반 GET 요청도 문제없이 경로를 파싱해야 한다.")
    void parseHttpRequestWithoutQueryString() throws Exception {
        // given: 일반적인 정적 파일 요청
        String requestMessage = "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n\r\n";
        BufferedReader br = new BufferedReader(new StringReader(requestMessage));

        // when
        HttpRequest request = HttpRequest.of(br);

        // then: Path에 물음표가 포함되지 않았는지, Map은 비어있는지 확인
        assertThat(request.getPath()).isEqualTo("/index.html");
        assertThat(request.getParameters()).isEmpty();
    }

    @Test
    @DisplayName("getCoreRequestInfo 호출 시 핵심 헤더 정보만 필터링하여 문자열로 반환해야 한다.")
    void testGetCoreRequestInfo() throws Exception {
        // given: 다양한 헤더가 포함된 요청
        String requestMessage = "GET / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Connection: keep-alive\r\n" +
                "Accept: text/html\r\n" +
                "Cookie: some-cookie=value\r\n\r\n";
        BufferedReader br = new BufferedReader(new StringReader(requestMessage));

        // when
        HttpRequest request = HttpRequest.of(br);
        String coreInfo = request.getCoreRequestInfo();

        // then: 지정한 핵심 헤더는 포함되고, 그 외의 헤더(Cookie 등)는 필터링되었는지 확인
        assertThat(coreInfo).contains("GET / HTTP/1.1");
        assertThat(coreInfo).contains("Host: localhost:8080");
        assertThat(coreInfo).contains("Connection: keep-alive");
        assertThat(coreInfo).contains("Accept: text/html");
        assertThat(coreInfo).doesNotContain("Cookie");
    }
}