package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtil;
import util.MimeType;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static Logger logger = LoggerFactory.getLogger(HttpResponse.class);
    private DataOutputStream dos;

    // TODO: 경로를 하드코딩하는 대신, 서버 설정 정보에서 가져오거나 생성자에서 주입받는 방식을 고민해 보세요.
    private static final String BASIC_PATH = "./src/main/resources/static";
    private final Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(String path, byte[] body) throws IOException {
        String contentType = MimeType.getMime(path);
        responseBody("200 OK", contentType, body);
    }

    public void sendNotFound() throws IOException {
        // TODO: 만약 404.html 파일 자체가 물리적으로 존재하지 않는다면 어떤 일이 벌어질까요?
        // 그런 예외 상황에 대한 '최후의 방어선(기본 텍스트 응답 등)'을 생각해 보세요.
        File file = new File(BASIC_PATH + "/error/404.html");
        byte[] body = FileUtil.readFile(file);
        responseBody("404 Not Found", "text/html", body);
    }

    public void sendRedirect(String url) throws IOException {
        // [분석] 302 응답 시에는 바디(Body)가 필요 없으므로 헤더까지만 전송하고 끝내는 것이 HTTP 규격에 맞습니다.
        dos.writeBytes("HTTP/1.1 302 Found\r\n");
        dos.writeBytes("Location: " + url + "\r\n");

        writeHeader();

        dos.writeBytes("\r\n");
        dos.flush();
    }

    public void serveFile(String path) throws IOException {
        File file = new File(BASIC_PATH + path);
        if (file.exists() && file.isFile()) {
            byte[] body = FileUtil.readFile(file);
            forward(path, body);
        } else {
            sendNotFound();
            logger.error("파일을 찾을 수 없습니다: {}", path);
        }
    }

    private void responseBody(String status, String contentType, byte[] body) throws IOException {
        dos.writeBytes("HTTP/1.1 " + status + "\r\n");
        dos.writeBytes("Content-Type: " + contentType + "\r\n");
        // TODO: body.length는 바이트 길이입니다. 만약 본문에 다국어가 섞인다면 Content-Length가 정확히 계산되는지 확인이 필요합니다.
        dos.writeBytes("Content-Length: " + body.length + "\r\n");

        writeHeader();

        dos.writeBytes("\r\n");
        dos.write(body, 0, body.length);
        dos.flush();
    }

    private void writeHeader() throws IOException {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            dos.writeBytes(header.getKey() + ": " + header.getValue() + "\r\n");
        }
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
}