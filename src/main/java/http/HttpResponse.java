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
        File file = new File(BASIC_PATH + "/error/404.html");
        byte[] body = FileUtil.readFile(file);
        responseBody("404 Not Found", "text/html", body);
    }

    public void sendRedirect(String url) throws IOException {
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