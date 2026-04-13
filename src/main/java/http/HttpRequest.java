package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static utils.HttpConstant.CRLF;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String protocol;
    private final HttpHeaders headers;
    private final Map<String, String> parameters;

    private static final String QUERY_SEPARATOR = "\\?";
    private static final Character KEY_VALUE_SEPARATOR = '=';
    private static final Character PARAMETER_SEPARATOR = '&';

    private HttpRequest(String method, String path, String protocol,
                        Map<String, String> headers, Map<String, String> parameters) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = new HttpHeaders(headers);
        this.parameters = parameters;
    }

    public static HttpRequest of(BufferedReader br) throws IOException {
        List<String> startLine = Arrays.stream(br.readLine().split(" ")).toList();
        String method = startLine.get(0);
        String originPath = startLine.get(1);

        String[] pathSplits = originPath.split(QUERY_SEPARATOR);
        String path = pathSplits[0];
        Map<String, String> params = pathSplits.length > 1 ? extractPathAndParams(pathSplits[1]) : new HashMap<>();

        String protocol = startLine.get(2);

        Map<String, String> headers = extractHeaders(br);

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
    private static Map<String, String> extractHeaders(BufferedReader br) throws IOException {
        Map<String, String> result = new HashMap<>();

        String line;
        while((line = br.readLine()) != null) {
            if(line.isEmpty())
                break;

            String[] header = line.split(":", 2);
            result.put(header[0].trim(), header[1].trim());
        }

        return result;
    }

    public String getCoreRequestInfo(){
        return method + " " + path + " " + protocol + " " + CRLF +
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
}
