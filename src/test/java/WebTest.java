 // 원재님의 실제 패키지 명으로 수정하세요!

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.WebServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class WebTest {
    private static final Logger logger = LoggerFactory.getLogger(WebTest.class);
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void startServer() {
        // 서버를 별도의 가상 스레드에서 실행하여 테스트 코드와 동시에 돌아가게 합니다.
        Executors.newVirtualThreadPerTaskExecutor().execute(() -> {
            try {
                logger.info("테스트용 서버 가동 중...");
                WebServer.main(new String[]{});
            } catch (Exception e) {
                logger.error("서버 가동 실패: {}", e.getMessage());
            }
        });

        // 서버가 부팅될 시간을 잠시 줍니다 (1초)
        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Test
    @DisplayName("1. 메인 페이지(/) 요청 시 index.html 내용을 반환해야 한다")
    void shouldReturnIndexHtmlForRootPath() throws IOException, InterruptedException {
        // given
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/"))
                .GET()
                .build();

        // when
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).isPresent().get().asString().contains("text/html");
        assertThat(response.body()).contains("<!DOCTYPE html>"); // index.html 특유의 태그 확인
    }

    @Test
    @DisplayName("2. 특정 정적 파일(/css/styles.css) 요청 시 해당 파일을 반환해야 한다")
    void shouldReturnStaticFile() throws IOException, InterruptedException {
        // given
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/main.css"))
                .GET()
                .build();

        // when
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isNotEmpty(); // CSS 내용이 비어있지 않은지 확인
    }
}