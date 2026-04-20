package webserver.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class HttpRequestTest {

    @Test
    @DisplayName("GET 요청 시 URI의 쿼리 스트링이 정상적으로 Map에 파싱되어야 한다.")
    void parseGetRequestWithQuery() throws Exception {
        // given: 쿼리 스트링이 포함된 GET 요청 메세지
        String requestMessage =
                "GET /users?name=jjjkuul&age=30 HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "Connection: keep-alive\r\n\r\n";
        InputStream in = new ByteArrayInputStream(requestMessage.getBytes());

        // when: 파싱 실행
        HttpRequest request = HttpRequest.of(in);

        // then: queries Map에 데이터가 정확히 들어갔는지 검증
        assertThat(request.getQueries()).hasSize(2)
                .containsEntry("name", "jjjkuul")
                .containsEntry("age", "30");
        // Body는 비어있어야 함
        assertThat(request.getBodies()).isEmpty();
    }

    @Test
    @DisplayName("POST 요청 시 Content-Length만큼 바디를 읽어 Map에 파싱해야 한다.")
    void parsePostRequestWithBody() throws Exception {
        // given: 본문(Body) 데이터가 있는 POST 요청
        String bodyData = "userId=myId&password=myPassword";
        String requestMessage =
                "POST /users/create HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "Content-Length: " + bodyData.length() + "\r\n" + // 정확한 길이 명시
                        "Content-Type: application/x-www-form-urlencoded\r\n\r\n" +
                        bodyData;
        InputStream in = new ByteArrayInputStream(requestMessage.getBytes());

        // when
        HttpRequest request = HttpRequest.of(in);

        // then: bodies Map에 데이터가 파싱되었는지 검증
        assertThat(request.getBodies()).hasSize(2)
                .containsEntry("userId", "myId")
                .containsEntry("password", "myPassword");
    }

    @Test
    @DisplayName("회원가입 폼에서 값을 비워 보냈을 때(Value 없음), 에러 없이 빈 문자열로 파싱되어야 한다.")
    void parseEmptyValueInQueryOrBody() throws Exception {
        // given: hobby 값이 비어있는 엣지 케이스 (TODO로 남겨두셨던 문제의 상황!)
        String bodyData = "userId=jjjkuul&hobby=";
        String requestMessage =
                "POST /users/create HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "Content-Length: " + bodyData.length() + "\r\n\r\n" +
                        bodyData;
        InputStream in = new ByteArrayInputStream(requestMessage.getBytes());

        // when & then: ArrayIndexOutOfBoundsException이 터지지 않고 정상 통과해야 함
        HttpRequest request = assertDoesNotThrow(() -> HttpRequest.of(in));

        // 빈 문자열("")로 잘 들어갔는지 검증
        assertThat(request.getBodies()).containsEntry("userId", "jjjkuul")
                .containsEntry("hobby", "");
    }
}