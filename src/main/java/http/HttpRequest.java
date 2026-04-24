package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);
    private String method;
    private String path;
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();
            if (line == null) return;

            parseRequestLine(line);
            parseHeader(br);

            if ("POST".equals(method)) {
                parseBody(br);
            }

        } catch (IOException e) {
            logger.error("HTTP 요청 분석 에러: {}", e.getMessage());
        }
    }

    private void parseRequestLine(String requestLine) {
        String[] tokens = requestLine.split(" ");
        if (tokens.length < 2) return;

        this.method = tokens[0];
        String fullPath = tokens[1];

        int index = fullPath.indexOf("?");
        if (index != -1) {
            this.path = fullPath.substring(0, index);
            String queryString = fullPath.substring(index + 1);
            this.params.putAll(Parser.parseQueryString(queryString));
        } else {
            this.path = fullPath;
        }
    }

    private void parseHeader(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            logger.debug("Header 정보: {}", line);

            int index = line.indexOf(":");
            if (index != -1) {
                String key = line.substring(0, index).trim().toLowerCase();
                String value = line.substring(index + 1).trim();
                headers.put(key, value);

                if ("cookie".equals(key)) {
                    Parser.parseCookies(value, cookies);
                }
            }
        }
    }

    private void parseBody(BufferedReader br) throws IOException {
        String lengthStr = headers.get("content-length");
        if (lengthStr == null) return;

        int length = Integer.parseInt(lengthStr);
        char[] body = new char[length];
        int readLength = br.read(body, 0, length);

        String bodyData = new String(body, 0, readLength);
        this.params.putAll(Parser.parseQueryString(bodyData));
    }

    public String getParameter(String key) { return params.get(key); }
    public String getCookie(String name) { return cookies.get(name); }
    public String getMethod() { return method; }
    public String getPath() { return path; }
}