package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class WebServerTest {

    @Test
    @DisplayName("서버가 지정된 포트로 정상 구동되고, 실제 HTTP 요청에 응답해야 한다.")
    void serverStartsAndRespondsToRequest() throws Exception {
        // given: 8080번은 우리가 직접 띄울 때 써야 하니, 테스트 전용 포트(8888)를 지정합니다.
        String[] args = {"8888"};

        // 메인 스레드가 멈추지 않도록, 별도의 백그라운드 스레드에서 서버를 실행합니다!
        Thread serverThread = new Thread(() -> {
            try {
                WebServer.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // 서버 소켓이 열릴 때까지 아주 잠깐(0.5초) 기다려줍니다.
        Thread.sleep(500);

        // when: 자바에 내장된 '진짜' HTTP 클라이언트를 사용해 브라우저처럼 요청을 쏩니다.
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8888/index.html"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // then: 서버가 200 상태 코드를 주고, 바디에 HTML 내용이 담겨 왔는지 확인합니다!
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isNotEmpty();
        assertThat(response.body().toLowerCase()).contains("<html"); // index.html 내용 일부 확인

        // 🧹 (주의) 테스트가 끝나면 다른 테스트를 위해 스레드를 멈춰줘야 합니다.
        // 현재 WebServer 코드에는 우아한 종료(Graceful Shutdown) 기능이 없어서 강제로 멈춥니다.
        serverThread.interrupt();
    }
}