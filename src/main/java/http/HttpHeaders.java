package http;

import java.util.HashMap;
import java.util.Map;

import static utils.HttpConstant.CRLF;

public class HttpHeaders {
    private final Map<String, String> headers;

    public HttpHeaders(Map<String, String> headers) {
        Map<String, String> lowerCaseHeaders = new HashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            lowerCaseHeaders.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        this.headers = Map.copyOf(lowerCaseHeaders);
    }

    public String getHeader(String key){
        return headers.get(key.toLowerCase());
    }

    public String getOneLineHeaderInfo(String key){
        String value = getHeader(key);
        return value != null ? (key + ": " + value + CRLF) : "";
    }
}
