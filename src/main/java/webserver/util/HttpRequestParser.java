package webserver.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import webserver.HttpRequest;

public class HttpRequestParser {
    public HttpRequest parse(BufferedInputStream in) throws IOException {
        RequestLine requestLine = parseRequestLine(in);
        Map<String, String> headers = parseHeaders(in);
        byte[] body = parseBody(in, headers);
        Map<String, String> bodyMap = body != null && body.length > 0
                ? parseUrlEncodedParams(new String(body, UTF_8))
                : new HashMap<>();

        return new HttpRequest(requestLine.method, requestLine.path, requestLine.protocol,
                requestLine.queryParameters, headers, body, bodyMap);
    }

    private RequestLine parseRequestLine(BufferedInputStream in) throws IOException {
        String requestLine = readLine(in);
        if (requestLine == null) {
            throw new IOException("EOF: No request line received");
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid request line: " + requestLine);
        }

        String method = parts[0];
        String[] pathParts = parts[1].split("\\?");
        String path = pathParts[0];
        String protocol = parts[2];

        Map<String, String> queryParameters = pathParts.length > 1 ? parseQueryParameters(pathParts[1]) : Map.of();
        return new RequestLine(method, path, protocol, queryParameters);
    }

    private Map<String, String> parseQueryParameters(String queryString) {
        return parseUrlEncodedParams(queryString);
    }

    private Map<String, String> parseUrlEncodedParams(String queryString) {
        Map<String, String> parsed = new HashMap<>();
        for (String param : queryString.split("&")) {
            String[] keyAndValue = param.split("=");
            String key = URLDecoder.decode(keyAndValue[0], UTF_8);
            String value = keyAndValue.length > 1 ? URLDecoder.decode(keyAndValue[1], UTF_8) : "";
            parsed.put(key, value);
        }
        return parsed;
    }

    private Map<String, String> parseHeaders(BufferedInputStream in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) { // 헤더 바디 구분 공백까지
            String[] keyAndValue = line.split(":", 2);
            headers.put(keyAndValue[0].trim(), keyAndValue[1].trim());
        }
        return headers;
    }

    private byte[] parseBody(BufferedInputStream in, Map<String, String> headers) throws IOException {
        if (!headers.containsKey("Content-Length")) {
            return null;
        }

        int contentLength = Integer.parseInt(headers.get("Content-Length"));
        byte[] bodyBytes = new byte[contentLength];
        int readLength = in.readNBytes(bodyBytes, 0, contentLength);

        if (contentLength != readLength) {
            throw new IOException("Body truncated. Expected " + contentLength + " bytes, got " + readLength);
        }
        return bodyBytes;
    }

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

    private record RequestLine(String method, String path, String protocol, Map<String, String> queryParameters) {
    }
}
