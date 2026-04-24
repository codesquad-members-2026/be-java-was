package webserver;

import core.webserver.RequestHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestHandlerTest {
    @Test
    @DisplayName("클라이언트의 정상적인 HTTP 요청이 들어오면, 소켓의 OutputStream에 응답이 정상적으로 기록되어야 한다.")
    void requestHandler_IntegrationTest() throws Exception {
        // given: 1. 껍데기뿐인 가짜 소켓을 만듭니다.
        Socket mockSocket = mock(Socket.class);

        // given: 2. 브라우저가 보낼 법한 HTTP 요청 메세지를 직접 문자열로 작성합니다.
        String requestMessage =
                "GET / HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "Connection: keep-alive\r\n" +
                        "Accept: text/html\r\n\r\n";

        // 문자열을 바이트로 변환해 가짜 입력 스트림에 넣습니다. (우리가 만든 HttpRequest.of 가 읽을 대상)
        InputStream in = new ByteArrayInputStream(requestMessage.getBytes());

        // given: 3. 서버의 응답을 가로챌 빈 도화지(출력 스트림)를 준비합니다.
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // given: 4. 가짜 소켓에게 "누가 스트림 달라고 하면 우리가 준비한 걸 줘!" 라고 지시(Stubbing)합니다.
        when(mockSocket.getInputStream()).thenReturn(in);
        when(mockSocket.getOutputStream()).thenReturn(out);

        // 테스트할 타겟 객체 생성
        RequestHandler requestHandler = new RequestHandler(mockSocket);

        // when: 스레드의 핵심 로직을 직접 실행합니다. (멀티 스레딩 환경이 아니므로 run() 직접 호출)
        requestHandler.run();

        // then: 빈 도화지(out)에 응답 데이터가 잘 쓰였는지 확인합니다.
        String response = out.toString();

        // 디버깅용 출력 (실제 눈으로 확인해보세요!)
        System.out.println("=== 서버가 소켓에 쓴 응답 메시지 ===");
        System.out.println(response);

        // 검증: 우리가 만든 Router와 HttpResponse가 정상 작동했다면, HTTP/1.1 로 시작해야 합니다.
        assertThat(response).startsWith("HTTP/1.1");
        // 루트(/) 경로를 요청했으므로 200 OK 나 302 Found 등이 응답되었는지 확인 (Router 구현에 따라 다름)
        // assertThat(response).contains("200 OK");
    }
}
