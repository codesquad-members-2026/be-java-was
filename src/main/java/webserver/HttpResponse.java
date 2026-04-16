package webserver;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HttpResponse {
    private static final String CRLF = "\r\n";

    private final DataOutputStream dos;
    private final ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
    private int statusCode = 200;
    private String contentType = "text/html; charset=UTF-8";

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void write(String string) throws IOException {
        bodyBuffer.write(string.getBytes(UTF_8));
    }

    public void write(byte[] bytes) throws IOException {
        bodyBuffer.write(bytes);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void flush() throws IOException {
        int contentLength = bodyBuffer.size();
        dos.writeBytes("HTTP/1.1 " + statusCode + " " + getResponsePhrase(statusCode) + CRLF);
        dos.writeBytes("Content-Type: " + contentType + CRLF);
        dos.writeBytes("Content-Length: " + contentLength + CRLF);
        dos.writeBytes(CRLF);
        dos.write(bodyBuffer.toByteArray());
        dos.flush();
    }

    private String getResponsePhrase(int statusCode) {
        //todo: 상태코드 분리
        return switch (statusCode) {
            case 200 -> "OK";
            case 302 -> "Found";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown Status";
        };
    }

}
