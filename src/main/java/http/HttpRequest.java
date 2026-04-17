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

        } catch (IOException e) {
            logger.error("HTTP 요청 분석 에러: {}", e.getMessage());
        }
    }

    private void parseRequestLine(String requestLine) {
        String[] split = requestLine.split(" ");
        // TODO: split 배열의 크기가 예상한 대로(최소 2개 이상) 들어왔는지 검증하는 코드가 있으면 더 안전합니다.
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

            // TODO: HTTP 헤더 키(Content-Length, Cookie 등)는 대소문자를 구분하지 않습니다(Case-insensitive).
            // 현재 startsWith는 대문자로 시작할 때만 작동하는데, 이 부분을 어떻게 유연하게 바꿀 수 있을지 고민해 보세요.
            if (line.toLowerCase().startsWith("content-length:")) {
                this.contentlength = Integer.parseInt(line.split(":")[1].trim());
            }

            if (line.startsWith("Cookie:")) {
                String real = line.substring(7).trim();
                Parser.parseCookies(real, cookies);
            }
        }
    }

    private void parseBody(BufferedReader br) throws IOException {
        // TODO (중요): Content-Length는 바이트(Byte) 수입니다. 하지만 char[]는 문자(Char) 수입니다.
        // UTF-8 환경에서 한글은 3바이트를 차지하는데, 이때 바이트 길이만큼 char[]로 읽으면 어떤 문제가 생길까요?
        // 바이트 단위로 정확히 읽어올 수 있는 방법을 찾아보세요.
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