package webserver.request;

import java.util.Map;

import static utils.HttpConstant.CRLF;

public class RequestHeaders {
    private final Map<String, String> headers;
    private final String sessionId;

    public RequestHeaders(Map<String, String> headers) {
        this.headers = headers;
        this.sessionId = extractSessionId(headers.get("cookie"));
    }

    private String extractSessionId(String cookies){
        if(cookies == null || cookies.isEmpty()){
            return "";
        }

        String[] cookiesArray = cookies.split(";");
        for(String cookie : cookiesArray){
            if(cookie.trim().startsWith("JSESSIONID")){
                return cookie.split("=")[1];
            }
        }

        return "";
    }

    public String getHeader(String key){
        return headers.get(key.toLowerCase());
    }
    public String getOneLineHeaderInfo(String key){
        String value = getHeader(key);
        return value != null ? (key + ": " + value + CRLF) : "";
    }
    public String getSessionId(){
        return sessionId;
    }
}
