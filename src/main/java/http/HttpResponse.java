package http;

import util.MimeType;
import java.io.*;

public class HttpResponse {
    private DataOutputStream dos;

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(String path, byte[] body) throws IOException {
        // 1. 확장자에 따라 MIME Type 결정 (아까 만든 로직 활용!)
        String contentType = MimeType.getMime(path);

        // 2. 헤더 작성
        dos.writeBytes("HTTP/1.1 200 OK \r\n");
        dos.writeBytes("Content-Type: " + contentType + "\r\n"); // 이게 핵심!
        dos.writeBytes("Content-Length: " + body.length + "\r\n");
        dos.writeBytes("\r\n");

        // 3. 본문 전송
        dos.write(body, 0, body.length);
        dos.flush();
    }

    public void sendRedirect(String url) throws IOException {
        dos.writeBytes("HTTP/1.1 302 Found\r\n");
        dos.writeBytes("Location: " + url + "\r\n");
        dos.writeBytes("\r\n");
        dos.flush();
    }

}