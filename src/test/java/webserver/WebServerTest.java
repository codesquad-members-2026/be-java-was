package webserver;

import org.junit.jupiter.api.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;

public class WebServerTest {

    private static Thread serverThread;

    @BeforeAll
    static void startTestThread(){
        serverThread = new Thread(() -> {
            try {
                WebServer.main(new String[] {"8081"});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        serverThread.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void kilServer(){
        serverThread.interrupt();
    }

    @Test
    void testHttpRequestToIndexHtml() throws Exception {
        URL url = new URL("http://127.0.0.1:8081");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        assertThat(conn.getResponseCode()).isEqualTo(200);
        assertThat(conn.getContentType()).isEqualTo("text/html;charset=utf-8");
    }

}
