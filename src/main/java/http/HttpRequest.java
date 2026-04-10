package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String protocol;
    private final Map<String, String> headers;
    private final Map<String, String> parameters;

    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private static final String CRLF = "\r\n";
    private static final String QUERY_SEPARATOR = "\\?";
    private static final Character KEY_VALUE_SEPARATOR = '=';
    private static final Character PARAMETER_SEPARATOR = '&';

    private HttpRequest(String method, String path, String protocol,
                        Map<String, String> headers, Map<String, String> parameters) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = headers;
        this.parameters = parameters;
    }

    // TODO: 왜 정적 팩토리 메서드로 객체를 반환하는가? 파싱 작업이 많이 일어나기 때문? -> 생성자에서 복잡한 연산이 이루어져서는 안됌
    public static HttpRequest of(BufferedReader br) throws IOException {
        List<String> startLine = Arrays.stream(br.readLine().split(" ")).toList();
        String method = startLine.get(0);

        String[] pathSplits = startLine.get(1).split(QUERY_SEPARATOR);
        String path = pathSplits[0];
        Map<String, String> params = new HashMap<>();
        if(pathSplits.length > 1){
            params = extractPathAndParams(pathSplits[1]);
        }

        String protocol = startLine.get(2);

        Map<String, String> headers = new HashMap<>();
        String line;
        while((line = br.readLine()) != null) {
            if(line.isEmpty())
                break;

            String[] header = line.split(":", 2);
            headers.put(header[0].trim(), header[1].trim());
        }

        return new HttpRequest(method, path, protocol, headers, params);
    }

    private static Map<String, String> extractPathAndParams(String query) {
        String[] queryParts = query.split(String.valueOf(PARAMETER_SEPARATOR));
        Map<String, String> params = new HashMap<>();

        for(String queryPart : queryParts){
            String[] keyValue = queryPart.split(String.valueOf(KEY_VALUE_SEPARATOR));
            String key = keyValue[0];
            String value = decodeValue(keyValue[1]);
            params.put(key, value);
        }

        return params;
    }
    private static String decodeValue(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    public String getCoreRequestInfo(){
        StringBuilder coreRequestInfo = new StringBuilder();
        coreRequestInfo.append(method).append(" ").append(path).append(" ").append(protocol).append(" ").append(CRLF);
        headers.forEach((k, v) -> {
            if(k.equals("Host") || k.equals("Accept") || k.equals("Connection")){
                coreRequestInfo.append(k).append(": ").append(v).append(CRLF);
            }
        });

        return coreRequestInfo.toString();
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
}
