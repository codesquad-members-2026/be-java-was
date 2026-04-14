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

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();
            if (line == null) return;

            parseRequestLine(line);
            parseHeader(br);
        } catch (IOException e) {
            logger.error("HTTP 요청 분석 에러: {}", e.getMessage());
        }
    }

    private void parseRequestLine(String requestLine) {
        String[] split = requestLine.split(" ");
        this.method = split[0];
        String fullPath = split[1];
        this.path = fullPath;

        if (fullPath.contains("?")) {
            String[] pathSplit = fullPath.split("\\?");
            this.path = pathSplit[0];

            if (pathSplit.length > 1) {
                this.params = Parser.parseQueryString(pathSplit[1]);
            }
        }
    }
    public String getParameter(String key) {
        return params.get(key);
    }

    private void parseHeader(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            logger.debug("Header 정보: {}", line);
        }
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
}