package webserver.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static utils.HttpConstant.CRLF;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String protocol;
    private final HttpHeaders headers;
    private final Map<String, String> bodies;
    private final Map<String, String> parameters;

    private static final String QUERY_SEPARATOR = "\\?";
    private static final Character KEY_VALUE_SEPARATOR = '=';
    private static final Character PARAMETER_SEPARATOR = '&';

    private HttpRequest(String method, String path, String protocol,
                        Map<String, String> headers, Map<String, String> bodies, Map<String, String> parameters) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = new HttpHeaders(headers);
        this.bodies = bodies;
        this.parameters = parameters;
    }

    public static HttpRequest of(BufferedReader br) throws IOException {
        List<String> startLine = Arrays.stream(br.readLine().split(" ")).toList();
        String method = startLine.get(0);
        String originPath = startLine.get(1);

        String[] pathSplits = originPath.split(QUERY_SEPARATOR);
        String path = pathSplits[0];
        Map<String, String> params = pathSplits.length > 1 ? splitQuery(pathSplits[1]) : new HashMap<>();

        String protocol = startLine.get(2);

        Map<String, String> allMessages = extractHttpMessage(br);
        String body = allMessages.get("body");
        Map<String, String> bodies = body.isEmpty() ? new HashMap<>() : splitQuery(body);
        allMessages.remove("body");
        return new HttpRequest(method, path, protocol, allMessages, bodies, params);
    }

    // TODO: 회원가입 폼 데이터 중 빈칸이 들어오면 ArrayIndexOutOfBoundsException 발생
    private static Map<String, String> splitQuery(String query) {
        String[] queryParts = query.split(String.valueOf(PARAMETER_SEPARATOR));
        Map<String, String> result = new HashMap<>();

        for(String queryPart : queryParts){
            String[] keyValue = queryPart.split(String.valueOf(KEY_VALUE_SEPARATOR));
            String key = keyValue[0];
            String value = decodeValue(keyValue[1]);
            result.put(key, value);
        }

        return result;
    }
    private static String decodeValue(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
    private static Map<String, String> extractHttpMessage(BufferedReader br) throws IOException {
        Map<String, String> allMessages = extractHeaders(br);

        String contentLengthStr = allMessages.getOrDefault("Content-Length", "0").trim();
        int contentLength = Integer.parseInt(contentLengthStr);
        String body = extractBody(br, contentLength);
        allMessages.put("body", body);

        return allMessages;
    }
    // TODO: String.lines() / NIO - ByteBuffer 로 바꿔보기
    private static Map<String, String> extractHeaders(BufferedReader br) throws IOException {
        Map<String, String> headers = new HashMap<>();

        String line = br.readLine();
        while(line != null && !line.isEmpty()) {
            String[] header = line.split(":", 2);
            headers.put(header[0].trim(), header[1].trim());

            line = br.readLine();
        }

        return headers;
    }
    private static String extractBody(BufferedReader br, int contentLength) throws IOException {
        String body = "";

        if(contentLength > 0){
            char[] bodyBuffer = new char[contentLength];
            br.read(bodyBuffer, 0, contentLength);

            body = new String(bodyBuffer);
        }

        return body;
    }

    public String getStartLineInfo(){
        return method + " " + path + " " + protocol + " " + CRLF;
    }
    public String getCoreRequestInfo(){
        return getStartLineInfo() +
                headers.getOneLineHeaderInfo("host") +
                headers.getOneLineHeaderInfo("accept") +
                headers.getOneLineHeaderInfo("connection");
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
    public Map<String, String> getParameters() {
        return parameters;
    }
    public Map<String, String> getBodies() {
        return bodies;
    }
}
