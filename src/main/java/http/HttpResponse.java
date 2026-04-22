package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
    private final DataOutputStream dos;
    private final Map<String, String> headers = new HashMap<>();
    private String status = "200 OK";
    private byte[] body = new byte[0];

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setBody(String html) {
        this.body = html.getBytes(StandardCharsets.UTF_8);
        addHeader("Content-Type", "text/html;charset=utf-8");
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void sendRedirect(String url) {
        this.status = "302 Found";
        addHeader("Location", url);
    }

    public void send() {
        try {
            dos.writeBytes("HTTP/1.1 " + status + " \r\n");
            if (body.length > 0) {
                addHeader("Content-Length", String.valueOf(body.length));
            }

            for (Map.Entry<String, String> header : headers.entrySet()) {
                dos.writeBytes(header.getKey() + ": " + header.getValue() + "\r\n");
            }

            dos.writeBytes("\r\n");
            if (body.length > 0) {
                dos.write(body, 0, body.length);
            }
            dos.flush();
        } catch (IOException e) {
            logger.error("응답 전송 중 예외 발생: {}", e.getMessage());
        }
    }
}