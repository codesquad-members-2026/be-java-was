package webserver.http;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import webserver.session.Cookie;

public class HttpResponse {
    private static final String CRLF = "\r\n";

    private final DataOutputStream dos;
    private final ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
    private HttpStatus statusCode = HttpStatus.OK;
    private String contentType = Mime.HTML.getContentType();
    private String location;
    private final List<Cookie> cookies = new ArrayList<>();

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public void sendRedirect(String location) {
        setStatusCode(HttpStatus.FOUND);
        this.location = location;
    }

    public void write(byte[] bytes) throws IOException {
        bodyBuffer.write(bytes);
    }

    public void write(String string) throws IOException {
        bodyBuffer.write(string.getBytes(StandardCharsets.UTF_8));
    }

    public void reset() {
        bodyBuffer.reset();
    }
    public void flush() throws IOException {
        int contentLength = bodyBuffer.size();
        dos.writeBytes("HTTP/1.1 " + statusCode.toString() + CRLF);
        if (statusCode == HttpStatus.FOUND) {
            dos.writeBytes("Location: " + location + CRLF);
        }
        dos.writeBytes("Content-Type: " + contentType + CRLF);
        dos.writeBytes("Content-Length: " + contentLength + CRLF);
        for (Cookie cookie : cookies) {
            dos.writeBytes("Set-Cookie: " + cookie.toHeaderValue() + CRLF);
        }
        dos.writeBytes(CRLF);
        dos.write(bodyBuffer.toByteArray());
        dos.flush();
    }

}
