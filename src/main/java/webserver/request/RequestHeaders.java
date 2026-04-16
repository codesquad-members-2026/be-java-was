package webserver.request;

import java.util.HashMap;
import java.util.Map;

import static utils.HttpConstant.CRLF;

public class RequestHeaders {
    private final Map<String, String> headers;

    public RequestHeaders(Map<String, String> headers) {
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
    // TODO: JVM 싱크 문제 해결
    public String getSessionId(){
        String cookies; // TODO: 여기에 Breaking Point 걸면 그냥 넘어감
        cookies = headers.get("cookie");

        if(cookies == null)
            return "";

        String[] cookiesArray = cookies.split(";");

        for(String cookie : cookiesArray){
            if(cookie.startsWith("JSESSIONID")){
                return cookie.split("=")[1];
            }
        }

        return "";
    }
}
