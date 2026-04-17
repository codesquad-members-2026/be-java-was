package webserver;

import static java.nio.charset.StandardCharsets.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method; // todo: enum 고려
    private String path;
    private String protocol;
    private byte[] body;
    private final Map<String, String> queryParameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    private HttpRequest(BufferedInputStream in) throws IOException {
        parseRequestLine(in);
        parseHeaders(in);
        parseBody(in);
    }

    public static HttpRequest from(BufferedInputStream in) throws IOException {
        return new HttpRequest(in);
    }

    private void parseRequestLine(BufferedInputStream in) throws IOException {
        String requestLine = readLine(in);
        if (requestLine == null) {
            throw new IOException("EOF: No request line received");
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid request line: " + requestLine);
        }

        method = parts[0];
        String[] pathParts = parts[1].split("\\?");
        path = pathParts[0];
        protocol = parts[2];

        if (pathParts.length > 1) {
            parseQueryParameters(pathParts[1]);
        }
    }

    public void parseUrlEncodedParams(String queryString, Map<String, String> parsed) {
        for (String param : queryString.split("&")) {
            String[] keyAndValue = param.split("=");
            String key = URLDecoder.decode(keyAndValue[0], UTF_8);
            String value = keyAndValue.length > 1 ? URLDecoder.decode(keyAndValue[1], UTF_8) : "";
            parsed.put(key, value);
        }
    }

    private void parseQueryParameters(String queryString) {
        parseUrlEncodedParams(queryString, queryParameters);
    }

    private void parseHeaders(BufferedInputStream in) throws IOException {
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) { // 헤더 바디 구분 공백까지
            String[] keyAndValue = line.split(":", 2);
            headers.put(keyAndValue[0].trim(), keyAndValue[1].trim());
        }
    }

    private void parseBody(BufferedInputStream in) throws IOException {
        if (!headers.containsKey("Content-Length")) {
            return;
        }

        int contentLength = Integer.parseInt(headers.get("Content-Length"));
        byte[] bodyBytes = new byte[contentLength];
        int readLength = in.readNBytes(bodyBytes, 0, contentLength);

        if (contentLength != readLength) {
            throw new IOException("Body truncated. Expected " + contentLength + " bytes, got " + readLength);
        }
        body = bodyBytes;
    }

    // todo: 파서 분리 고려
    private String readLine(BufferedInputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int prev = -1;
        int curr;
        while ((curr = in.read()) != -1) {
            if (prev == '\r' && curr == '\n') {
                byte[] bytes = buffer.toByteArray();
                return new String(bytes, 0, bytes.length - 1, UTF_8);
            }
            buffer.write(curr);
            prev = curr;
        }
        if (buffer.size() == 0) {
            return null;
        }

        throw new IOException("Unexpected EOF: line not terminated with CRLF");
    }


    public boolean isStaticResource() {
        // todo: 확장자 분리하기
        return method.equals("GET") && (path.endsWith(".html") || path.endsWith(".css")
                || path.endsWith(".ico") || path.endsWith(".svg"));
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", method, path, protocol);
    }

}
