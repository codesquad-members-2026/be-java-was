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
    private int contentlength;
    private Map<String, String> cookies = new HashMap<>();

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();
            if (line == null) return;

            parseRequestLine(line);
            parseHeader(br);

            if ("POST".equals(method) && contentlength > 0) {
                parseBody(br);
            }

            if (line.startsWith("Cookie:")) {
                String real = line.substring(7).trim();
                Parser.parseCookies(real, cookies);
            }

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

    private void parseHeader(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            logger.debug("Header 정보: {}", line);

            if (line.toLowerCase().startsWith("content-length:")) {
                this.contentlength = Integer.parseInt(line.split(":")[1].trim());
            }
        }
    }

    private void parseBody(BufferedReader br) throws IOException {
        char[] body = new char[contentlength];
        br.read(body, 0, contentlength);

        String bodyData = new String(body);
        logger.debug("Body 데이터: {}", bodyData);

        Map<String, String> bodyParams = Parser.parseQueryString(bodyData);
        this.params.putAll(bodyParams);
    }


    public String getCookie(String name) {return cookies.get(name);}
    public String getParameter(String key) {return params.get(key);}
    public String getMethod() { return method; }
    public String getPath() { return path; }
}