package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RequestHandlerTest {

    @Test
    @DisplayName("기본 경로(/) 요청 시 index.html을 반환해야 한다")
    void testDefaultPath() throws IOException {
        // Given: "/" 경로로 GET 요청을 보내는 상황 설정
        String request = "GET / HTTP/1.1\r\nHost: localhost\r\n\r\n";
        byte[] response = invokeRequestHandler(request);
        String responseString = new String(response, StandardCharsets.UTF_8);

        // Then: 헤더에 text/html이 포함되어 있고, 응답 코드가 200 OK인지 확인
        assertThat(responseString).contains("HTTP/1.1 200 OK");
        assertThat(responseString).contains("Content-Type: text/html;charset=utf-8");
    }

    @Test
    @DisplayName("CSS 파일 요청 시 정확한 MIME 타입을 반환해야 한다")
    void testCssMimeType() throws IOException {
        // Given: ".css" 파일 요청
        String request = "GET /main.css HTTP/1.1\r\n\r\n";
        byte[] response = invokeRequestHandler(request);
        String responseString = new String(response, StandardCharsets.UTF_8);

        // Then: Content-Type이 text/css인지 확인
        assertThat(responseString).contains("Content-Type: text/css;charset=utf-8");
    }

    @Test
    @DisplayName("확장자가 없는 폴더 경로(/login) 요청 시 index.html로 보정되어야 한다")
    void testDirectoryPath() throws IOException {
        // Given: "/login" (점도 없고 슬래시도 없음) 요청
        String request = "GET /login HTTP/1.1\r\n\r\n";
        byte[] response = invokeRequestHandler(request);
        String responseString = new String(response, StandardCharsets.UTF_8);

        // Then: 최종적으로 index.html 내용이 포함되어야 함 (Content-Type으로 간접 확인)
        assertThat(responseString).contains("Content-Type: text/html;charset=utf-8");
    }

    /**
     * RequestHandler에 가짜 소켓을 주입하여 실행 결과를 받아오는 헬퍼 메서드
     */
    private byte[] invokeRequestHandler(String request) throws IOException {
        Socket socket = mock(Socket.class);
        InputStream in = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);

        RequestHandler handler = new RequestHandler(socket);
        handler.run();

        return out.toByteArray();
    }
}