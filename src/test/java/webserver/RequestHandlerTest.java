package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

class RequestHandlerTest {

    @Test
    @DisplayName("유효한 정적 파일(index.html) 요청 시, Socket의 OutputStream으로 200 OK 응답이 쓰여야 한다.")
    void handleValidStaticFileRequest() throws Exception {
        // given: 1. 브라우저가 보낸 척하는 가짜 HTTP 요청 데이터
        String requestMessage = "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n\r\n";
        InputStream in = new ByteArrayInputStream(requestMessage.getBytes());

        // given: 2. RequestHandler가 뱉어낼 응답을 담아둘 빈 바구니
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // given: 3. 입출력 스트림을 갈아끼운 가짜 소켓(Mock Socket) 생성
        Socket mockSocket = new Socket() {
            @Override public InputStream getInputStream() { return in; }
            @Override public OutputStream getOutputStream() { return out; }
            @Override public InetAddress getInetAddress() { return InetAddress.getLoopbackAddress(); }
            @Override public int getPort() { return 8080; }
        };

        // when: 가짜 소켓을 쥐여주고 run() 실행! (실제 네트워크 연결 없음)
        RequestHandler handler = new RequestHandler(mockSocket);
        handler.run();

        // then: 바구니(out)에 담긴 서버의 응답 메세지를 문자열로 꺼내서 검증
        String responseMessage = out.toString();

        // 1. 상태 코드가 정상적인가?
        assertThat(responseMessage).startsWith("HTTP/1.1 200 OK");
        // 2. 헤더가 잘 들어갔는가?
        assertThat(responseMessage).contains("Content-Type: text/html");
        assertThat(responseMessage).contains("Content-Length:");
    }

    @Test
    @DisplayName("존재하지 않는 파일 요청 시, 404 Not Found 응답이 쓰여야 한다.")
    void handleNotFoundRequest() throws Exception {
        // given: 없는 파일 경로 요청
        String requestMessage = "GET /ghost-page.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n\r\n";
        InputStream in = new ByteArrayInputStream(requestMessage.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Socket mockSocket = new Socket() {
            @Override public InputStream getInputStream() { return in; }
            @Override public OutputStream getOutputStream() { return out; }
            @Override public InetAddress getInetAddress() { return InetAddress.getLoopbackAddress(); }
            @Override public int getPort() { return 8080; }
        };

        // when
        new RequestHandler(mockSocket).run();

        // then
        String responseMessage = out.toString();
        assertThat(responseMessage).startsWith("HTTP/1.1 404 Not Found");
    }
}