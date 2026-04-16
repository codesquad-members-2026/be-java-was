package webserver;

import static java.nio.charset.StandardCharsets.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private String protocol;
    private final Map<String, String> queryParameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    private HttpRequest(BufferedReader reader) throws IOException {
        parseRequestLine(reader);
        parseHeaders(reader);
        // TODO: body 읽기
        //int contentLength = Integer.parseInt(header.get("Content-Length"));
    }

    private void parseRequestLine(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
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

    private void parseQueryParameters(String queryString) {
        for (String param : queryString.split("&")) {
            String[] keyAndValue = param.split("=");
            String key = URLDecoder.decode(keyAndValue[0], UTF_8);
            String value = keyAndValue.length > 1 ? URLDecoder.decode(keyAndValue[1], UTF_8) : "";
            queryParameters.put(key, value);
        }
    }

    private void parseHeaders(BufferedReader reader) throws IOException {
        String line;
        while (!(line = reader.readLine()).isEmpty()) { // 헤더 바디 구분 공백까지
            String[] keyAndValue = line.split(":");
            headers.put(keyAndValue[0].trim(), keyAndValue[1].trim());
        }
    }

    public static HttpRequest from(BufferedReader reader) throws IOException {
        return new HttpRequest(reader);
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

    @Override
    public String toString() {
        return String.format("%s %s %s", method, path, protocol);
    }

}
